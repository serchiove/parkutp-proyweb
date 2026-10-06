const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const path = require('node:path');

function pantalla(estado = { capacidad: 10, ocupados: 8, disponibles: 2, activo: true }) {
  const elementos = new Map(), consultas = [];
  function elemento(id = '') {
    return {
      id, dataset: {}, style: {}, attributes: {}, children: [], listeners: {}, textContent: '', value: '',
      addEventListener(event, listener) { this.listeners[event] = listener; },
      setAttribute(key, value) { this.attributes[key] = value; }, removeAttribute(key) { delete this.attributes[key]; },
      replaceChildren() { this.children = []; }, append(child) { this.children.push(child); }
    };
  }
  const byId = id => { if (!elementos.has(id)) elementos.set(id, elemento(id)); return elementos.get(id); };
  byId('operacion').dataset = { id: '1', api: '/api/estacionamientos/1' };
  byId('origen').value = 'SIMULADOR';
  const botones = ['ENTRADA', 'SALIDA'].map(tipo => { const b = elemento(); b.dataset.tipo = tipo; return b; });
  const entorno = { estado, movimientos: [], errorHistorial: false, resolverHistorial: null };
  vm.runInNewContext(fs.readFileSync(path.join(__dirname, '../src/main/resources/static/js/operacion.js'), 'utf8'), {
    document: { getElementById: byId, querySelectorAll: () => botones, createElement: () => elemento() },
    localStorage: { getItem: () => null, setItem() { }, removeItem() { } },
    Intl, Date, URLSearchParams, AbortSignal, crypto: { randomUUID: () => 'evento-prueba' },
    FormData: class { constructor() { return [['fecha', byId('filtro-fecha').value], ['placa', byId('filtro-placa').value], ['tipo', byId('filtro-tipo').value]]; } },
    setInterval(callback) { entorno.actualizar = callback; },
    async fetch(url, options) {
      consultas.push(url);
      if (options.method === 'POST') return { ok: true, json: async () => JSON.parse(options.body) };
      if (url.includes('/movimientos')) {
        if (entorno.resolverHistorial) return entorno.resolverHistorial(url);
        if (entorno.errorHistorial) throw new Error('Sin conexión');
        return { ok: true, json: async () => entorno.movimientos };
      }
      if (!entorno.estado) throw new Error('Sin conexión');
      return { ok: true, json: async () => entorno.estado };
    }
  });
  return { ...entorno, entorno, byId, botones, consultas };
}
const esperar = () => new Promise(resolve => setImmediate(resolve));

test('aforo muestra porcentaje, aviso y color según ocupación', async () => {
  const p = pantalla(); await esperar();
  assert.equal(p.byId('porcentaje').textContent, '80 %');
  assert.equal(p.byId('avance-aforo').style.width, '80%');
  assert.match(p.byId('avance-aforo').className, /bg-warning/);
  assert.match(p.byId('detalle-aforo').textContent, /Ocupación alta/);
  for (const [ocupados, color] of [[0, 'success'], [10, 'danger']]) {
    p.entorno.estado = { capacidad: 10, ocupados, disponibles: 10 - ocupados, activo: true };
    await p.entorno.actualizar(); assert.match(p.byId('avance-aforo').className, new RegExp('bg-' + color));
  }
  assert.equal(p.botones[0].disabled, true); assert.equal(p.botones[1].disabled, false);
});
test('vacío e inactivo restringen los accesos correspondientes', async () => {
  const p = pantalla({ capacidad: 10, ocupados: 0, disponibles: 10, activo: true }); await esperar();
  assert.equal(p.botones[1].disabled, true); assert.match(p.byId('restriccion').textContent, /vacío/);
  p.entorno.estado = { capacidad: 10, ocupados: 1, disponibles: 9, activo: false }; await p.entorno.actualizar();
  assert.equal(p.botones[0].disabled, true); assert.equal(p.botones[1].disabled, false);
  assert.match(p.byId('avance-aforo').className, /bg-secondary/);
});
test('filtros se conservan durante actualización y limpiar los elimina', async () => {
  const p = pantalla(); await esperar();
  p.byId('filtro-fecha').value = '2026-10-06'; p.byId('filtro-placa').value = ' abc '; p.byId('filtro-tipo').value = 'ENTRADA';
  p.byId('filtros').listeners.submit({ preventDefault() { }, currentTarget: p.byId('filtros') }); await esperar();
  await p.entorno.actualizar();
  const url = new URL(p.consultas.at(-1), 'http://localhost');
  assert.equal(url.searchParams.get('fecha'), '2026-10-06'); assert.equal(url.searchParams.get('placa'), 'abc'); assert.equal(url.searchParams.get('tipo'), 'ENTRADA');
  p.byId('filtros').listeners.reset(); await esperar(); assert.equal(p.consultas.at(-1), '/api/estacionamientos/1/movimientos');
  assert.match(p.byId('resultado-historial').textContent, /No hay movimientos/);
});
test('respuesta antigua no sobrescribe una búsqueda más reciente', async () => {
  const p = pantalla(); await esperar(); let resolver;
  p.entorno.resolverHistorial = () => new Promise(resolve => { resolver = resolve; });
  p.byId('filtros').listeners.submit({ preventDefault() { }, currentTarget: p.byId('filtros') });
  p.entorno.resolverHistorial = null; p.byId('filtros').listeners.reset(); await esperar();
  resolver({ ok: true, json: async () => [{ registradoEn: '2026-10-06T12:00:00Z', tipo: 'ENTRADA', origen: 'MANUAL', placa: 'VIEJA' }] }); await esperar();
  assert.match(p.byId('resultado-historial').textContent, /No hay movimientos/);
});
test('fallo del historial no impide operar con aforo confirmado', async () => {
  const p = pantalla(); await esperar(); p.entorno.errorHistorial = true; await p.entorno.actualizar();
  assert.equal(p.botones[0].disabled, false); assert.match(p.byId('resultado-historial').textContent, /No se pudo consultar/);
  p.entorno.estado = null; await p.entorno.actualizar(); assert.equal(p.botones[0].disabled, true);
  assert.equal(p.byId('porcentaje').textContent, 'Sin confirmar');
  assert.equal(p.byId('barra-aforo').attributes['aria-valuenow'], undefined);
});
test('confirma el tipo y la placa del movimiento registrado', async () => {
  const p = pantalla(); await esperar(); p.byId('placa').value = 'abc-123';
  p.botones[0].listeners.click(); await esperar();
  assert.equal(p.byId('aviso').textContent, 'Entrada confirmada · Placa ABC-123.');
});
