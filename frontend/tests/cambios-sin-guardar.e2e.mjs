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
  const { page, context, go } = await session();
  await go('/sedes');
  await page.locator('#sede-nombre').fill('Borrador sede');

  await page.locator('nav a[href="/estacionamientos"]').click();
  await page.getByRole('dialog').waitFor();
  await page.getByRole('button', { name: 'Seguir editando', exact: true }).click();
  await page.waitForURL('**/sedes');
  assert.equal(await page.locator('#sede-nombre').inputValue(), 'Borrador sede');
  check('Cancelar la salida conserva el borrador de la sede');

  await page.locator('nav a[href="/estacionamientos"]').click();
  await page.getByRole('button', { name: 'Salir sin guardar', exact: true }).click();
  await page.waitForURL('**/estacionamientos');
  check('Aceptar permite cambiar de pantalla');
  await page.locator('#zona-nombre').fill('Borrador zona');

  await page.locator('nav a[href="/sedes"]').click();
  await page.getByRole('dialog').waitFor();
  await page.getByRole('button', { name: 'Seguir editando', exact: true }).click();
  assert.equal(await page.locator('#zona-nombre').inputValue(), 'Borrador zona');
  check('El estacionamiento también conserva los cambios al cancelar');

  await page.locator('nav a[href="/sedes"]').click();
  await page.getByRole('button', { name: 'Salir sin guardar', exact: true }).click();
  await page.waitForURL('**/sedes');
  await page.locator('nav a[href="/estacionamientos"]').click();
  await page.waitForURL('**/estacionamientos');
  check('Un formulario sin cambios permite salir sin confirmación');
  await context.close();
} finally {
  await browser.close();
  await new Promise(resolve => server.close(resolve));
}