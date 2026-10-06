(() => {
 'use strict';
 const root=document.getElementById('operacion'), api=root.dataset.api;
 const byId=id=>document.getElementById(id), buttons=[...document.querySelectorAll('.paso')];
 const storageKey='parkutp-pendiente-'+root.dataset.id;
 let pendiente=null, enviando=false, estado=null;
 try { pendiente=JSON.parse(localStorage.getItem(storageKey)); } catch (_) { /* almacenamiento opcional */ }
 function guardar() { try { pendiente?localStorage.setItem(storageKey,JSON.stringify(pendiente)):localStorage.removeItem(storageKey); } catch (_) {} }
 function aviso(texto,clase='info') { const el=byId('aviso');el.className='alert alert-'+clase;el.textContent=texto; }
 function controles() {
  buttons.forEach(b=>b.disabled=enviando || !!pendiente || !estado || (b.dataset.tipo==='ENTRADA'?(!estado.activo || estado.disponibles<=0):estado.ocupados<=0));
  byId('reintentar').hidden=!pendiente;byId('reintentar').disabled=enviando;
 }
 async function request(url,options={}) {
  const response=await fetch(url,{...options,signal:AbortSignal.timeout(10000)});
  const json=await response.json();
  if(!response.ok) { const error=new Error((json.errores||[json.mensaje||'No se pudo completar la operación']).join('. '));error.http=true;throw error; }
  return json;
 }
 async function actualizar() {
  try {
   const [e,movimientos]=await Promise.all([request(api),request(api+'/movimientos')]);estado=e;
   ['capacidad','ocupados','disponibles'].forEach(k=>byId(k).textContent=e[k]);
   const luz=byId('luz');luz.className='status '+(!e.activo?'status-off':e.disponibles>0?'status-open':'status-full');luz.textContent=!e.activo?'Inactivo':e.disponibles>0?'Luz verde · Hay espacios':'Luz roja · Lleno';
   byId('conexion').textContent='Actualizado '+new Intl.DateTimeFormat('es-PE',{timeZone:'America/Lima',timeStyle:'medium'}).format(new Date());
   const tbody=byId('historial');tbody.replaceChildren();
   for(const m of movimientos) { const tr=document.createElement('tr');for(const text of [new Intl.DateTimeFormat('es-PE',{timeZone:'America/Lima',dateStyle:'short',timeStyle:'medium'}).format(new Date(m.registradoEn)),m.tipo,m.origen,m.placa||'—']) { const td=document.createElement('td');td.textContent=text;tr.append(td); }tbody.append(tr); }
  } catch (_) { estado=null;byId('conexion').textContent='Sin conexión · Los valores pueden estar desactualizados';byId('luz').className='status status-off';byId('luz').textContent='Aforo sin confirmar'; }
  controles();
 }
 async function enviar() {
  if(enviando || !pendiente) return;
  enviando=true;controles();
  try { await request(api+'/movimientos',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(pendiente)});pendiente=null;guardar();aviso('Movimiento registrado.','success'); }
  catch(error) { if(error.http) { pendiente=null;guardar();aviso(error.message,'danger'); } else aviso('No se confirmó el registro. Reintenta el mismo evento para evitar duplicados.','warning'); }
  finally { enviando=false;await actualizar(); }
 }
 buttons.forEach(button=>button.addEventListener('click',()=>{ if(enviando || pendiente) return;pendiente={eventoId:crypto.randomUUID(),tipo:button.dataset.tipo,origen:byId('origen').value,placa:byId('placa').value.trim().toUpperCase()||null};guardar();enviar(); }));
 byId('reintentar').addEventListener('click',enviar);
 if(pendiente) aviso('Hay un evento pendiente de confirmación. Reintenta el mismo evento.','warning');
 actualizar();setInterval(actualizar,5000);
})();
