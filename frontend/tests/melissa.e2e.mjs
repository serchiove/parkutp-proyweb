// Recorrido Angular con API simulada. No certifica persistencia en PostgreSQL.
import assert from 'node:assert/strict';
import { createRequire } from 'node:module';
import { mkdir } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import { crearServidor } from '../serve-ricardo.mjs';
const { chromium } = createRequire(import.meta.url)(process.env.PLAYWRIGHT_MODULE ?? 'playwright');
const server = crearServidor(fileURLToPath(new URL('../dist/frontend/browser/', import.meta.url)));
await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
const base = `http://127.0.0.1:${server.address().port}`;
const browser = await chromium.launch({ ...(process.env.PLAYWRIGHT_CHANNEL ? { channel: process.env.PLAYWRIGHT_CHANNEL } : {}), headless: true });
const checks = [];
const check = message => { checks.push(message); console.log('OK:', message); };
const output = new URL('../../docs/melissa/evidencias/frontend-simulado/', import.meta.url);
await mkdir(output, { recursive: true });

async function session(rol = 'ADMIN') {
  const context = await browser.newContext({ viewport: { width: 1440, height: 1000 }, locale: 'es-PE' });
  const page = await context.newPage();
  const state = { sedes: [{ id: 'AQP', nombre: 'Arequipa' }], zonas: [], writes: [], failList: false, conflict: false, forbidden: false, expired: false };
  const errors = [];
  page.on('pageerror', error => errors.push(error.message));
  await page.route('https://fonts.googleapis.com/**', route => route.abort());
  await page.route('http://localhost:8081/api/**', async route => {
    const req = route.request(), path = new URL(req.url()).pathname, method = req.method();
    const json = (data, status = 200) => route.fulfill({ status, contentType: 'application/json', body: JSON.stringify(data) });
    if (path.endsWith('/auth/login')) return json({ token: 'token-simulado', tipo: 'Bearer', usuario: rol.toLowerCase(), rol, expiraEnSegundos: 3600 });
    assert.equal(req.headers()['authorization'], 'Bearer token-simulado');
    if (state.expired) return json({ mensaje: 'Autenticación requerida' }, 401);
    if (method === 'GET') {
      if (path.endsWith('/sedes')) {
        if (state.failList) return json({ mensaje: 'No disponible' }, 503);
        return json(state.sedes);
      }
      if (path.endsWith('/estacionamientos')) return json(state.zonas);
      if (path.endsWith('/movimientos') || path.endsWith('/accesos')) return json([]);
      return json(state.zonas[0] ?? null);
    }
    state.writes.push({ method, path, data: method === 'DELETE' ? null : req.postDataJSON() });
    if (state.forbidden || rol !== 'ADMIN') return json({ mensaje: 'No tienes permiso' }, 403);
    if (state.conflict) return json({ mensaje: 'Registro asociado: no se puede eliminar' }, 409);
    const id = decodeURIComponent(path.split('/').at(-1));
    if (path.includes('/sedes')) {
      if (method === 'DELETE') { state.sedes = state.sedes.filter(x => x.id !== id); return route.fulfill({ status: 204 }); }
      const data = req.postDataJSON();
      if (method === 'POST' && state.sedes.some(x => x.id === data.id)) return json({ mensaje: 'Ya existe una sede con ese código o nombre' }, 409);
      const sede = { ...data, id: method === 'PUT' ? id : data.id };
      state.sedes = method === 'PUT' ? state.sedes.map(x => x.id === id ? sede : x) : [...state.sedes, sede];
      return json(sede, method === 'POST' ? 201 : 200);
    }
    if (method === 'DELETE') { state.zonas = state.zonas.filter(x => x.id !== Number(id)); return route.fulfill({ status: 204 }); }
    const data = req.postDataJSON();
    const zona = { ...data, id: method === 'PUT' ? Number(id) : 12, sedeNombre: state.sedes.find(x => x.id === data.sedeId)?.nombre, ocupados: 0 };
    state.zonas = method === 'PUT' ? state.zonas.map(x => x.id === Number(id) ? zona : x) : [...state.zonas, zona];
    return json(zona, method === 'POST' ? 201 : 200);
  });
  await page.goto(`${base}/login`);
  await page.getByLabel('Usuario', { exact: true }).fill(rol.toLowerCase());
  await page.getByLabel('Contraseña', { exact: true }).fill('solo-prueba-simulada');
  await page.getByRole('button', { name: 'Iniciar sesión' }).click();
  await page.waitForURL(rol === 'CONSULTA' ? '**/historial' : '**/operacion');
  const go = async path => { await page.evaluate(path => history.pushState({}, '', path), path); await page.evaluate(() => dispatchEvent(new PopStateEvent('popstate'))); await page.getByRole('heading', { name: path === '/sedes' ? 'Sedes' : 'Estacionamientos', exact: true }).waitFor(); };
  return { page, context, state, errors, go };
}
try {
  const { page, context, state, errors, go } = await session();
  await go('/sedes');
  await page.getByText('Arequipa', { exact: true }).waitFor();
  check('Carga de sedes usando el JWT del AuthService existente');
  await page.getByRole('button', { name: 'Crear sede', exact: true }).click();
  await page.locator('#sede-id-error').waitFor();
  assert.equal(state.writes.length, 0);
  await page.locator('#sede-id').fill('código inválido');
  await page.locator('#sede-nombre').fill('   ');
  await page.getByRole('button', { name: 'Crear sede', exact: true }).click();
  assert.equal(state.writes.length, 0);
  check('Formulario de sede vacío, código inválido y nombre en blanco no envían peticiones');
  await page.locator('#sede-id').fill('NUEVA');
  await page.locator('#sede-nombre').fill('  Sede nueva  ');
  await page.getByRole('button', { name: 'Crear sede', exact: true }).click();
  await page.getByRole('status').filter({ hasText: 'creado correctamente' }).waitFor();
  assert.deepEqual(state.writes.at(-1).data, { id: 'NUEVA', nombre: 'Sede nueva' });
  await page.getByRole('button', { name: 'Editar sede Sede nueva', exact: true }).click();
  assert.equal(await page.locator('#sede-id').isDisabled(), true);
  await page.locator('#sede-nombre').fill('Sede editada');
  await page.getByRole('button', { name: 'Guardar cambios', exact: true }).click();
  await page.getByRole('cell', { name: 'Sede editada', exact: true }).waitFor();
  assert.equal(state.writes.at(-1).method, 'PUT');
  check('Crear y editar sede conservando su código y normalizando el nombre');
  await page.locator('#sede-id').fill('AQP'); await page.locator('#sede-nombre').fill('Duplicada');
  await page.getByRole('button', { name: 'Crear sede', exact: true }).click();
  await page.getByRole('alert').filter({ hasText: 'Ya existe' }).waitFor();
  assert.equal(await page.locator('#sede-nombre').inputValue(), 'Duplicada');
  check('Error 409 de sede visible sin perder los datos del formulario');
  await page.getByRole('button', { name: 'Eliminar sede Sede editada', exact: true }).click();
  const beforeCancel = state.writes.length;
  await page.getByRole('button', { name: 'Cancelar eliminación', exact: true }).click();
  assert.equal(state.writes.length, beforeCancel);
  await page.getByRole('button', { name: 'Eliminar sede Sede editada', exact: true }).click();
  state.conflict = true;
  await page.getByRole('button', { name: 'Confirmar eliminación', exact: true }).click();
  await page.getByRole('alert').filter({ hasText: 'Registro asociado' }).waitFor();
  assert.equal(state.sedes.length, 2);
  state.conflict = false;
  await page.getByRole('button', { name: 'Confirmar eliminación', exact: true }).click();
  await page.getByRole('status').filter({ hasText: 'eliminado correctamente' }).waitFor();
  check('Eliminación de sede requiere confirmación, muestra conflictos y recarga listado');
  await page.screenshot({ path: fileURLToPath(new URL('01-sedes.png', output)), fullPage: true });
  state.failList = true;
  await page.getByRole('button', { name: 'Actualizar listado', exact: true }).click();
  await page.getByRole('alert').waitFor();
  assert.equal(await page.getByRole('button', { name: 'Crear sede', exact: true }).isDisabled(), true);
  state.failList = false;
  await page.getByRole('button', { name: 'Actualizar listado', exact: true }).click();
  await page.getByRole('cell', { name: 'Arequipa', exact: true }).waitFor();
  check('Fallo de carga bloquea el formulario y permite reintentar');
  await go('/estacionamientos');
  await page.getByText('No hay estacionamientos registrados.', { exact: true }).waitFor();
  await page.getByRole('button', { name: 'Crear estacionamiento', exact: true }).click();
  await page.locator('#zona-sede-error').waitFor();
  const beforeInvalid = state.writes.length;
  await page.locator('#zona-sede').selectOption('AQP'); await page.locator('#zona-nombre').fill('Zona Angular');
  for (const value of ['0', '10001', '1.5']) {
    await page.locator('#zona-capacidad').fill(value);
    await page.getByRole('button', { name: 'Crear estacionamiento', exact: true }).click();
    assert.equal(state.writes.length, beforeInvalid);
  }
  check('Valida sede, nombre y capacidad entera entre 1 y 10000');
  await page.locator('#zona-capacidad').fill('6');
  await page.getByRole('button', { name: 'Crear estacionamiento', exact: true }).click();
  await page.getByRole('cell', { name: 'Zona Angular', exact: true }).waitFor();
  assert.equal(state.writes.at(-1).data.capacidad, 6);
  await page.getByRole('button', { name: 'Editar estacionamiento Zona Angular', exact: true }).click();
  await page.getByLabel('Habilitado para recibir vehículos').uncheck();
  await page.getByRole('button', { name: 'Guardar cambios', exact: true }).click();
  await page.getByText('Inactivo', { exact: true }).waitFor();
  assert.equal(state.writes.at(-1).data.activo, false);
  check('Crear y actualizar estacionamiento enviando capacidad numérica y estado booleano');
  await page.screenshot({ path: fileURLToPath(new URL('02-estacionamientos.png', output)), fullPage: true });
  state.conflict = true;
  await page.getByRole('button', { name: 'Eliminar estacionamiento Zona Angular', exact: true }).click();
  await page.getByRole('button', { name: 'Confirmar eliminación', exact: true }).click();
  await page.getByRole('alert').filter({ hasText: 'Registro asociado' }).waitFor();
  assert.equal(state.zonas.length, 1);
  state.conflict = false;
  await page.getByRole('button', { name: 'Confirmar eliminación', exact: true }).click();
  await page.getByText('No hay estacionamientos registrados.', { exact: true }).waitFor();
  check('Conserva zona ante 409 y elimina al aceptar la API');
  state.forbidden = true;
  await page.locator('#zona-sede').selectOption('AQP'); await page.locator('#zona-nombre').fill('Rechazado');
  await page.getByRole('button', { name: 'Crear estacionamiento', exact: true }).click();
  await page.getByRole('alert').filter({ hasText: 'no tiene permiso' }).waitFor();
  check('Error 403 muestra mensaje de permisos');
  state.expired = true;
  await page.getByRole('button', { name: 'Actualizar listado', exact: true }).click();
  await page.waitForURL('**/login?expired=true');
  check('401 utiliza el interceptor existente y redirige al login');
  assert.deepEqual(errors, []); await context.close();
  for (const rol of ['OPERADOR', 'CONSULTA']) {
    const sessionData = await session(rol);
    for (const path of ['/sedes', '/estacionamientos']) {
      await sessionData.go(path);
      await sessionData.page.getByText('Solo ADMIN', { exact: false }).waitFor();
      assert.equal(await sessionData.page.locator('form').count(), 0);
      assert.equal(await sessionData.page.getByRole('button', { name: /^Editar|^Eliminar/ }).count(), 0);
    }
    assert.deepEqual(sessionData.errors, []); await sessionData.context.close();
    check(`${rol} no puede administrar ni al navegar directamente a las rutas`);
  }
  console.log(`${checks.length} comprobaciones aprobadas.`);
} finally { await browser.close(); await new Promise(resolve => server.close(resolve)); }
