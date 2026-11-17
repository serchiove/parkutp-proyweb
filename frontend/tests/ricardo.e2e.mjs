// Pruebas del frontend con API simulada. No certifican persistencia PostgreSQL.
import assert from 'node:assert/strict';
import { createRequire } from 'node:module';
import { mkdir, writeFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import { crearServidor } from '../serve-ricardo.mjs';
const require = createRequire(import.meta.url);
const { chromium } = require(process.env.PLAYWRIGHT_MODULE ?? 'playwright');
const output = new URL('../../docs/ricardo/evidencias/frontend-simulado/', import.meta.url);
await mkdir(output, { recursive: true });
const server = crearServidor(fileURLToPath(new URL('../dist/ricardo/browser/', import.meta.url)));
await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
const baseURL = `http://127.0.0.1:${server.address().port}`;
const browser = await chromium.launch({ channel: 'msedge', headless: true });
const results = [];
const check = (description) => { results.push(description); console.log('OK:', description); };

async function session(rol = 'ADMIN') {
  const context = await browser.newContext({ viewport: { width: 1440, height: 1050 }, locale: 'es-PE' });
  const page = await context.newPage();
  const errors = [];
  page.on('pageerror', e => errors.push(e.message));
  const state = {
    parques: [
      { id: 1, sedeId: 'A', sedeNombre: 'Arequipa', nombre: 'Estacionamiento Principal', capacidad: 5, activo: true, ocupados: 0 },
      { id: 2, sedeId: 'B', sedeNombre: 'Lima', nombre: 'Estacionamiento Norte', capacidad: 2, activo: false, ocupados: 0 },
    ],
    accesos: [{ id: 1, estacionamientoId: 1, nombre: 'Ingreso principal', tipo: 'ENTRADA' }],
    movimientos: [], events: new Map(), posts: [], requests: [],
    failList: false, expire: false, rejectDelete: false, loseResponse: false, failAforo: false, delayHistory: false,
  };
  await page.route('https://fonts.googleapis.com/**', route => route.abort());
  await page.route('http://localhost:8081/api/**', async route => {
    const request = route.request();
    const url = new URL(request.url());
    const path = url.pathname;
    const method = request.method();
    const send = (status, body) => route.fulfill({ status, contentType: 'application/json', body: JSON.stringify(body) });
    if (path.endsWith('/auth/login')) return send(200, { token: 'token-de-prueba', tipo: 'Bearer', expiraEnSegundos: 3600, usuario: rol.toLowerCase(), rol });
    assert.equal(request.headers()['authorization'], 'Bearer token-de-prueba');
    state.requests.push({ path, method, search: url.search });
    if (state.expire) return send(401, { mensaje: 'Sesión vencida' });
    if (path === '/api/estacionamientos') {
      if (state.failList) return route.abort('connectionfailed');
      return send(200, state.parques);
    }
    const match = path.match(/^\/api\/estacionamientos\/(\d+)(?:\/(accesos|movimientos)(?:\/(\d+))?)?$/);
    if (!match) return send(404, { mensaje: 'Ruta no prevista en la prueba' });
    const id = Number(match[1]);
    const parking = state.parques.find(e => e.id === id);
    if (!parking) return send(404, { mensaje: 'El estacionamiento no existe' });
    if (!match[2]) return state.failAforo ? route.abort('connectionfailed') : send(200, parking);
    if (match[2] === 'accesos') {
      const accesoId = Number(match[3]);
      if (method === 'GET') return send(200, state.accesos.filter(a => a.estacionamientoId === id));
      if (rol !== 'ADMIN') return send(403, { mensaje: 'Acceso denegado' });
      if (method === 'DELETE') {
        if (state.rejectDelete) return send(409, { mensaje: 'El acceso tiene movimientos o dispositivos asociados; conserva su historial' });
        state.accesos = state.accesos.filter(a => a.id !== accesoId);
        return route.fulfill({ status: 204 });
      }
      const data = request.postDataJSON();
      if (method === 'POST') {
        if (state.accesos.some(a => a.estacionamientoId === id && a.tipo === data.tipo)) return send(409, { mensaje: 'Ya existe un acceso de ese tipo' });
        const acceso = { id: 10 + state.accesos.length, estacionamientoId: id, ...data };
        state.accesos.push(acceso);
        return send(201, acceso);
      }
      const acceso = state.accesos.find(a => a.id === accesoId);
      Object.assign(acceso, data);
      return send(200, acceso);
    }
    if (method === 'GET') {
      const rows = state.movimientos.filter(m => m.estacionamientoId === id
        && (!url.searchParams.get('placa') || m.placa?.includes(url.searchParams.get('placa')))
        && (!url.searchParams.get('tipo') || m.tipo === url.searchParams.get('tipo')));
      if (state.delayHistory && id === 1) await new Promise(resolve => setTimeout(resolve, 350));
      return send(200, rows.slice(0, 50));
    }
    const data = request.postDataJSON();
    state.posts.push(data);
    if (rol === 'CONSULTA') return send(403, { mensaje: 'Acceso denegado' });
    if (state.events.has(data.eventoId)) return send(200, state.events.get(data.eventoId));
    if (!state.accesos.some(a => a.estacionamientoId === id && a.tipo === data.tipo)) return send(409, { mensaje: 'No está configurado el acceso de ' + data.tipo });
    if (data.tipo === 'ENTRADA' && (!parking.activo || parking.ocupados >= parking.capacidad)) return send(409, { mensaje: 'El estacionamiento está inactivo o lleno' });
    const inside = state.movimientos.filter(m => m.estacionamientoId === id && m.placa === data.placa).reduce((n, m) => n + (m.tipo === 'ENTRADA' ? 1 : -1), 0);
    if (data.tipo === 'ENTRADA' && data.placa && inside) return send(409, { mensaje: `La placa ${data.placa} ya está dentro de este estacionamiento` });
    if (data.tipo === 'SALIDA' && !inside) return send(409, { mensaje: 'La placa no tiene una entrada pendiente en este estacionamiento' });
    parking.ocupados += data.tipo === 'ENTRADA' ? 1 : -1;
    const movimiento = { id: state.movimientos.length + 1, estacionamientoId: id, accesoId: data.tipo === 'ENTRADA' ? 1 : 11, ...data, registradoEn: '2026-11-17T12:10:00Z' };
    state.movimientos.unshift(movimiento);
    state.events.set(data.eventoId, movimiento);
    if (state.loseResponse) { state.loseResponse = false; return route.abort('connectionfailed'); }
    return send(200, movimiento);
  });
  await page.goto(`${baseURL}/operacion`);
  await page.getByLabel('Usuario', { exact: true }).fill('usuario-prueba');
  await page.getByLabel('Contraseña', { exact: true }).fill('clave-prueba');
  await page.getByRole('button', { name: 'Iniciar sesión' }).click();
  await page.getByRole('heading', { name: 'Operación', exact: true }).waitFor();
  await page.locator('#parking').selectOption('1');
  return { context, page, state, errors };
}

try {
  const { context, page, state, errors } = await session();
  check('Login original e interceptor: todas las solicitudes protegidas usan el JWT existente.');
  const nav = page.getByRole('navigation', { name: 'Gestión de estacionamientos' });
  await nav.getByRole('link', { name: 'Accesos', exact: true }).click();
  await page.getByRole('cell', { name: 'Ingreso principal', exact: true }).waitFor();
  await page.getByRole('button', { name: 'Crear acceso', exact: true }).click();
  await page.getByText('Escribe un nombre de 1 a 80 caracteres').waitFor();
  assert.equal(state.accesos.length, 1);
  check('Accesos: formulario vacío no envía datos.');
  await page.locator('#nombre').fill('  Salida lateral  ');
  await page.locator('#tipo').selectOption('SALIDA');
  await page.getByRole('button', { name: 'Crear acceso', exact: true }).click();
  await page.getByText('Acceso «Salida lateral» creado correctamente.').waitFor();
  assert.equal(state.accesos[1].nombre, 'Salida lateral');
  check('Accesos: creación por API y normalización de espacios.');
  await page.locator('#nombre').fill('Otra salida');
  await page.locator('#tipo').selectOption('SALIDA');
  await page.getByRole('button', { name: 'Crear acceso', exact: true }).click();
  await page.getByRole('alert').filter({ hasText: 'Ya existe un acceso' }).waitFor();
  check('Accesos: se muestra el conflicto 409 de tipo duplicado.');
  const row = page.getByRole('row').filter({ hasText: 'Salida lateral' });
  await row.getByRole('button', { name: 'Editar', exact: true }).click();
  assert.equal(await page.locator('#tipo').isDisabled(), true);
  await page.locator('#nombre').fill('Salida vehicular');
  await page.getByRole('button', { name: 'Guardar cambios' }).click();
  await page.getByText('Acceso «Salida vehicular» actualizado correctamente.').waitFor();
  check('Accesos: edición conserva el tipo original.');
  await page.evaluate(() => scrollTo(0, 0));
  await page.screenshot({ path: fileURL('01-accesos.png'), fullPage: true });
  state.rejectDelete = true;
  await page.getByRole('row').filter({ hasText: 'Salida vehicular' }).getByRole('button', { name: 'Eliminar', exact: true }).click();
  await page.getByRole('button', { name: 'Confirmar eliminación' }).click();
  await page.getByRole('alert').filter({ hasText: 'conserva su historial' }).waitFor();
  check('Accesos: confirmación y rechazo de eliminación con historial asociado.');
  state.rejectDelete = false;
  await page.getByRole('button', { name: 'Confirmar eliminación' }).click();
  await page.getByText('Acceso «Salida vehicular» eliminado correctamente.').waitFor();
  assert.equal(state.accesos.length, 1);
  check('Accesos: eliminación 204 actualiza el listado.');
  state.accesos.push({ id: 11, estacionamientoId: 1, nombre: 'Salida', tipo: 'SALIDA' });
  await nav.getByRole('link', { name: 'Operación', exact: true }).click();
  await page.locator('#placa').fill('@@');
  await page.getByRole('button', { name: 'Registrar movimiento' }).click();
  await page.getByText('La placa debe tener de 3 a 12 letras').waitFor();
  assert.equal(state.posts.length, 0);
  check('Operación: placa inválida no genera un movimiento.');
  await page.locator('#placa').fill('abc-123');
  await page.getByRole('button', { name: 'Registrar movimiento' }).click();
  await page.getByText('Ingreso registrado: ABC-123. Movimiento #1.').waitFor();
  assert.equal(state.parques[0].ocupados, 1);
  assert.equal(state.posts[0].origen, 'MANUAL');
  assert.match(state.posts[0].eventoId, /^[0-9a-f-]{36}$/);
  await page.getByText('Ocupación: 20 %', { exact: true }).waitFor();
  check('Operación: entrada normaliza placa, crea UUID y actualiza aforo.');
  await page.locator('#placa').fill('ABC-123');
  await page.getByRole('button', { name: 'Registrar movimiento' }).click();
  await page.getByRole('alert').filter({ hasText: 'ya está dentro' }).waitFor();
  assert.equal(state.parques[0].ocupados, 1);
  check('Operación: conflicto de placa duplicada se muestra sin cambiar el aforo.');
  await page.locator('#mov-tipo').selectOption('SALIDA');
  await page.getByRole('button', { name: 'Registrar movimiento' }).click();
  await page.getByText('Salida registrada: ABC-123. Movimiento #2.').waitFor();
  await page.getByText('Ocupación: 0 %', { exact: true }).waitFor();
  check('Operación: salida reduce ocupados y libera espacio.');
  state.loseResponse = true;
  await page.locator('#mov-tipo').selectOption('ENTRADA');
  await page.locator('#placa').fill('XYZ-987');
  await page.getByRole('button', { name: 'Registrar movimiento' }).click();
  await page.getByRole('button', { name: 'Reintentar envío pendiente' }).waitFor();
  assert.equal(state.parques[0].ocupados, 1);
  const original = state.posts.at(-1).eventoId;
  assert.equal(await page.locator('#parking').isDisabled(), true);
  await nav.getByRole('link', { name: 'Historial', exact: true }).click();
  await page.getByRole('heading', { name: 'Historial', exact: true }).waitFor();
  await nav.getByRole('link', { name: 'Operación', exact: true }).click();
  await page.getByRole('button', { name: 'Reintentar envío pendiente' }).waitFor();
  await page.getByRole('button', { name: 'Reintentar envío pendiente' }).click();
  await page.getByText('Ingreso registrado: XYZ-987. Movimiento #3.').waitFor();
  assert.equal(state.posts.at(-1).eventoId, original);
  assert.equal(state.parques[0].ocupados, 1);
  check('Operación: respuesta perdida, navegación y reintento conservan el UUID; un solo incremento.');
  state.failAforo = true;
  await page.locator('#placa').fill('QWE-456');
  await page.getByRole('button', { name: 'Registrar movimiento' }).click();
  await page.getByText('Ingreso registrado: QWE-456. Movimiento #4.').waitFor();
  await page.getByRole('alert').filter({ hasText: 'el aforo no se pudo actualizar' }).waitFor();
  assert.equal(await page.getByRole('button', { name: 'Reintentar envío pendiente' }).count(), 0);
  check('Operación: fallo al refrescar el aforo conserva éxito y no propone duplicar el envío.');
  state.failAforo = false;
  await page.getByRole('button', { name: 'Actualizar aforo', exact: true }).click();
  await page.getByText('Ocupación: 40 %', { exact: true }).waitFor();
  await page.evaluate(() => scrollTo(0, 0));
  await page.screenshot({ path: fileURL('02-operacion.png'), fullPage: true });
  state.parques[0].ocupados = 5;
  await page.getByRole('button', { name: 'Actualizar aforo', exact: true }).click();
  await page.getByText('Ocupación: 100 %', { exact: true }).waitFor();
  await page.locator('#placa').fill('FULL-1');
  await page.getByRole('button', { name: 'Registrar movimiento' }).click();
  await page.getByRole('alert').filter({ hasText: 'inactivo o lleno' }).waitFor();
  check('Operación: estacionamiento lleno presenta 100 % y muestra el rechazo del ingreso.');
  state.parques[0].ocupados = 2;
  await page.locator('#parking').selectOption('2');
  await page.getByText('El estacionamiento está inactivo.').waitFor();
  await page.locator('#placa').fill('OFF-1');
  await page.getByRole('button', { name: 'Registrar movimiento' }).click();
  await page.getByRole('alert').filter({ hasText: 'No está configurado el acceso de ENTRADA' }).waitFor();
  check('Operación: estacionamiento inactivo y error de acceso no configurado visibles.');
  await page.locator('#parking').selectOption('1');
  await nav.getByRole('link', { name: 'Historial', exact: true }).click();
  await page.getByRole('heading', { name: 'Movimientos (4)', exact: true }).waitFor();
  await page.locator('#fecha').fill('2026-11-17');
  await page.locator('#filtro-placa').fill('xyz');
  await page.locator('#filtro-tipo').selectOption('ENTRADA');
  await page.getByRole('button', { name: 'Buscar', exact: true }).click();
  await page.getByRole('heading', { name: 'Movimientos (1)', exact: true }).waitFor();
  const search = new URLSearchParams(state.requests.at(-1).search);
  assert.equal(search.get('fecha'), '2026-11-17');
  assert.equal(search.get('placa'), 'XYZ');
  assert.equal(search.get('tipo'), 'ENTRADA');
  await page.getByRole('cell', { name: 'XYZ-987', exact: true }).waitFor();
  await page.getByRole('cell', { name: /7:10:00/ }).waitFor();
  check('Historial: combina los tres parámetros y presenta la hora de Perú.');
  await page.getByRole('button', { name: 'Actualizar resultados' }).click();
  await page.getByRole('heading', { name: 'Movimientos (1)', exact: true }).waitFor();
  assert.equal(new URLSearchParams(state.requests.at(-1).search).get('placa'), 'XYZ');
  check('Historial: actualizar conserva filtros aplicados.');
  await page.evaluate(() => scrollTo(0, 0));
  await page.screenshot({ path: fileURL('03-historial.png'), fullPage: true });
  await page.locator('#filtro-placa').fill('NUNCA');
  await page.getByRole('button', { name: 'Buscar', exact: true }).click();
  await page.getByText('No hay movimientos para los filtros seleccionados.').waitFor();
  await page.getByRole('button', { name: 'Limpiar filtros' }).click();
  await page.getByRole('heading', { name: 'Movimientos (4)', exact: true }).waitFor();
  check('Historial: estado vacío y limpieza de filtros.');
  state.delayHistory = true;
  await page.getByRole('button', { name: 'Actualizar resultados' }).click();
  await page.locator('#parking').selectOption('2');
  await page.getByText('No hay movimientos para los filtros seleccionados.').waitFor();
  // Espera acotada deliberada para comprobar que la respuesta vieja no reemplaza la nueva.
  await page.waitForTimeout(500);
  assert.equal(await page.getByRole('cell', { name: 'XYZ-987', exact: true }).count(), 0);
  check('Historial: respuesta tardía de otro estacionamiento no contamina la selección actual.');
  await page.setViewportSize({ width: 390, height: 844 });
  await page.screenshot({ path: fileURL('04-movil.png'), fullPage: true });
  assert.equal(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth), true);
  check('Diseño móvil: sin desbordamiento horizontal de la página a 390 px.');
  state.failList = true;
  await page.getByRole('button', { name: 'Actualizar estacionamientos' }).click();
  await page.getByRole('alert').filter({ hasText: 'No se pudo conectar con la API' }).waitFor();
  state.failList = false;
  await page.getByRole('button', { name: 'Actualizar estacionamientos' }).click();
  await page.locator('#parking').selectOption('1');
  await page.getByRole('heading', { name: 'Movimientos (4)', exact: true }).waitFor();
  check('Conexión: error visible y recuperación al actualizar estacionamientos.');
  state.expire = true;
  await page.getByRole('button', { name: 'Actualizar resultados' }).click();
  await page.waitForURL(/\/login\?expired=true/);
  check('Sesión vencida: el interceptor original redirige al login.');
  assert.deepEqual(errors, []);
  await context.close();

  for (const rol of ['OPERADOR', 'CONSULTA']) {
    const s = await session(rol);
    await s.page.getByRole('navigation', { name: 'Gestión de estacionamientos' }).getByRole('link', { name: 'Accesos', exact: true }).click();
    await s.page.getByText('Puedes consultar los accesos.').waitFor();
    assert.equal(await s.page.getByRole('button', { name: 'Crear acceso', exact: true }).count(), 0);
    await s.page.getByRole('navigation', { name: 'Gestión de estacionamientos' }).getByRole('link', { name: 'Operación', exact: true }).click();
    if (rol === 'OPERADOR') {
      await s.page.getByRole('button', { name: 'Registrar movimiento' }).waitFor();
      await s.page.getByRole('button', { name: 'Registrar movimiento' }).click();
      await s.page.getByText('Ingreso registrado: sin placa. Movimiento #1.').waitFor();
      assert.equal(s.state.posts[0].placa, null);
    } else {
      await s.page.getByText('Tu rol permite consultar el aforo.').waitFor();
      assert.equal(await s.page.getByRole('button', { name: 'Registrar movimiento' }).count(), 0);
      await s.page.evaluate(() => scrollTo(0, 0));
      await s.page.screenshot({ path: fileURL('05-rol-consulta.png'), fullPage: true });
    }
    assert.deepEqual(s.errors, []);
    check(`Rol ${rol}: acciones visibles acordes con sus permisos${rol === 'OPERADOR' ? ' y registro sin placa' : ''}.`);
    await s.context.close();
  }
  await writeFile(new URL('resultados.json', output), JSON.stringify({ fecha: new Date().toISOString(), entorno: 'Frontend Angular con API simulada por Playwright; sin PostgreSQL', resultados: results }, null, 2));
  console.log(`${results.length} comprobaciones completadas. Capturas en docs/ricardo/evidencias/frontend-simulado.`);
} finally { await browser.close(); server.closeAllConnections(); server.close(); }

function fileURL(name) {
  return fileURLToPath(new URL(name, output));
}
