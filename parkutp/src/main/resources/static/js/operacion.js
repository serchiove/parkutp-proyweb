(() => {
    'use strict';
    const root = document.getElementById('operacion'), api = root.dataset.api;
    const byId = id => document.getElementById(id), buttons = [...document.querySelectorAll('.paso')];
    const storageKey = 'parkutp-pendiente-' + root.dataset.id;
    let pendiente = null, enviando = false, estado = null, filtros = new URLSearchParams(), consultaHistorial = 0, consultaAforo = 0;
    try { pendiente = JSON.parse(localStorage.getItem(storageKey)); } catch (_) { /* almacenamiento opcional */ }
    function guardar() { try { pendiente ? localStorage.setItem(storageKey, JSON.stringify(pendiente)) : localStorage.removeItem(storageKey); } catch (_) { } }
    function aviso(texto, clase = 'info') { const el = byId('aviso'); el.className = 'alert alert-' + clase; el.textContent = texto; }
    function controles() {
        buttons.forEach(b => b.disabled = enviando || !!pendiente || !estado || (b.dataset.tipo === 'ENTRADA' ? (!estado.activo || estado.disponibles <= 0) : estado.ocupados <= 0));
        byId('reintentar').hidden = !pendiente; byId('reintentar').disabled = enviando;
        byId('restriccion').textContent = !estado ? 'Esperando conexión para registrar movimientos.' : pendiente ? 'Confirma el evento pendiente antes de registrar otro.' : !estado.activo ? 'Estacionamiento inactivo: solo se permiten salidas de vehículos que estén dentro.' : estado.disponibles <= 0 ? 'Estacionamiento lleno: solo se permiten salidas.' : estado.ocupados <= 0 ? 'Estacionamiento vacío: registra una entrada antes de una salida.' : '';
    }
    async function request(url, options = {}) {
        const response = await fetch(url, { ...options, signal: AbortSignal.timeout(10000) });
        const json = await response.json();
        if (!response.ok) { const error = new Error((json.errores || [json.mensaje || 'No se pudo completar la operación']).join('. ')); error.http = true; throw error; }
        return json;
    }
    async function actualizarAforo() {
        const consulta = ++consultaAforo;
        try {
            const e = await request(api); if (consulta !== consultaAforo) return; estado = e;
            ['capacidad', 'ocupados', 'disponibles'].forEach(k => byId(k).textContent = e[k]);
            const porcentaje = Math.max(0, Math.min(100, 100 * e.ocupados / e.capacidad));
            byId('porcentaje').textContent = new Intl.NumberFormat('es-PE', { maximumFractionDigits: 1 }).format(porcentaje) + ' %';
            byId('barra-aforo').setAttribute('aria-valuenow', porcentaje.toFixed(1));
            const avance = byId('avance-aforo'); avance.style.width = porcentaje + '%'; avance.className = 'progress-bar ' + (!e.activo ? 'bg-secondary' : porcentaje >= 100 ? 'bg-danger' : porcentaje >= 80 ? 'bg-warning' : 'bg-success');
            byId('detalle-aforo').textContent = !e.activo ? 'Estacionamiento inactivo.' : e.disponibles <= 0 ? 'Capacidad completa. No hay espacios disponibles.' : porcentaje >= 80 ? 'Ocupación alta. Quedan ' + e.disponibles + ' espacios disponibles.' : 'Hay ' + e.disponibles + ' espacios disponibles.';
            const luz = byId('luz'); luz.className = 'status ' + (!e.activo ? 'status-off' : e.disponibles > 0 ? 'status-open' : 'status-full'); luz.textContent = !e.activo ? 'Inactivo' : e.disponibles > 0 ? 'Luz verde · Hay espacios' : 'Luz roja · Lleno';
            byId('conexion').textContent = 'Actualizado ' + new Intl.DateTimeFormat('es-PE', { timeZone: 'America/Lima', timeStyle: 'medium' }).format(new Date());
        } catch (_) { if (consulta !== consultaAforo) return; estado = null; byId('conexion').textContent = 'Sin conexión · Los valores pueden estar desactualizados'; byId('luz').className = 'status status-off'; byId('luz').textContent = 'Aforo sin confirmar'; byId('porcentaje').textContent = 'Sin confirmar'; byId('barra-aforo').removeAttribute('aria-valuenow'); byId('avance-aforo').className = 'progress-bar bg-secondary'; byId('detalle-aforo').textContent = 'El último aforo mostrado puede estar desactualizado.'; }
        controles();
    }
    async function actualizarHistorial() {
        const consulta = ++consultaHistorial;
        try {
            const movimientos = await request(api + '/movimientos' + (filtros.size ? '?' + filtros.toString() : ''));
            if (consulta !== consultaHistorial) return;
            const tbody = byId('historial'); tbody.replaceChildren();
            for (const m of movimientos) { const tr = document.createElement('tr'); for (const text of [new Intl.DateTimeFormat('es-PE', { timeZone: 'America/Lima', dateStyle: 'short', timeStyle: 'medium' }).format(new Date(m.registradoEn)), m.tipo, m.origen, m.placa || '—']) { const td = document.createElement('td'); td.textContent = text; tr.append(td); } tbody.append(tr); }
            byId('resultado-historial').textContent = movimientos.length ? movimientos.length + ' movimientos mostrados' + (movimientos.length === 50 ? ' · Se muestran los 50 más recientes. Ajusta los filtros para precisar la búsqueda.' : '.') : 'No hay movimientos que coincidan con la búsqueda.';
            if (!movimientos.length) { const tr = document.createElement('tr'), td = document.createElement('td'); td.colSpan = 4; td.textContent = 'Sin movimientos para estos filtros.'; tr.append(td); tbody.append(tr); }
        } catch (error) { if (consulta !== consultaHistorial) return; byId('historial').replaceChildren(); byId('resultado-historial').textContent = error.http ? error.message : 'No se pudo consultar el historial. Se reintentará automáticamente.'; }
    }
    async function actualizar() {
        await Promise.all([actualizarAforo(), actualizarHistorial()]);
    }
    async function enviar() {
        if (enviando || !pendiente) return;
        enviando = true; controles();
        try { const movimiento = await request(api + '/movimientos', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(pendiente) }); pendiente = null; guardar(); aviso((movimiento.tipo === 'ENTRADA' ? 'Entrada' : 'Salida') + ' confirmada' + (movimiento.placa ? ' · Placa ' + movimiento.placa : '') + '.', 'success'); }
        catch (error) { if (error.http) { pendiente = null; guardar(); aviso(error.message, 'danger'); } else aviso('No se confirmó el registro. Reintenta el mismo evento para evitar duplicados.', 'warning'); }
        finally { enviando = false; await actualizar(); }
    }
    buttons.forEach(button => button.addEventListener('click', () => { if (enviando || pendiente) return; pendiente = { eventoId: crypto.randomUUID(), tipo: button.dataset.tipo, origen: byId('origen').value, placa: byId('placa').value.trim().toUpperCase() || null }; guardar(); enviar(); }));
    byId('reintentar').addEventListener('click', enviar);
    byId('filtros').addEventListener('submit', event => {
        event.preventDefault(); filtros = new URLSearchParams();
        for (const [key, value] of new FormData(event.currentTarget)) { const texto = value.trim(); if (texto) filtros.set(key, texto); }
        byId('resultado-historial').textContent = 'Buscando movimientos…'; byId('historial').replaceChildren(); actualizarHistorial();
    });
    byId('filtros').addEventListener('reset', () => { filtros = new URLSearchParams(); byId('resultado-historial').textContent = 'Consultando historial…'; byId('historial').replaceChildren(); actualizarHistorial(); });
    if (pendiente) aviso('Hay un evento pendiente de confirmación. Reintenta el mismo evento.', 'warning');
    actualizar(); setInterval(actualizar, 5000);
})();
