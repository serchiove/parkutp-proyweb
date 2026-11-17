// Requiere Spring Boot iniciado contra una base de revisión separada.
// Crea y elimina solo su sede y zona. No modifica usuarios ni datos preexistentes.
import assert from 'node:assert/strict';
import { createRequire } from 'node:module';
import { fileURLToPath } from 'node:url';
import { mkdir } from 'node:fs/promises';
import { crearServidor } from '../serve-ricardo.mjs';
const { chromium, request } = createRequire(import.meta.url)(process.env.PLAYWRIGHT_MODULE ?? 'playwright');
assert.ok(process.env.MELISSA_DEMO_PASSWORD, 'Configura MELISSA_DEMO_PASSWORD localmente.');
const api = await request.newContext({ baseURL: 'http://localhost:8081' });
const login = await api.post('/api/auth/login', { data: { username: 'admin', password: process.env.MELISSA_DEMO_PASSWORD } });
assert.equal(login.status(), 200);
const headers = { Authorization: `Bearer ${(await login.json()).token}` };
const server = crearServidor(fileURLToPath(new URL('../dist/frontend/browser/', import.meta.url)));
await new Promise(resolve => server.listen(5310, '127.0.0.1', resolve));
const browser = await chromium.launch({ ...(process.env.PLAYWRIGHT_CHANNEL ? { channel: process.env.PLAYWRIGHT_CHANNEL } : {}), headless: true });
const context = await browser.newContext({ viewport: { width: 1440, height: 1000 }, locale: 'es-PE' });
const page = await context.newPage();
const errors = []; page.on('pageerror', error => errors.push(error.message));
const output = new URL('../../docs/melissa/evidencias/api-real/', import.meta.url);
await mkdir(output, { recursive: true });
const suffix = Date.now().toString(36).toUpperCase();
const sedeId = `MEL-${suffix}`, zonaNombre = `Zona Melissa ${suffix}`;
let zonaId;
async function go(path) {
  await page.evaluate(path => { history.pushState({}, '', path); dispatchEvent(new PopStateEvent('popstate')); }, path);
  await page.getByRole('heading', { name: path === '/sedes' ? 'Sedes' : 'Estacionamientos', exact: true }).waitFor();
}
try {
  await page.goto('http://127.0.0.1:5310/login');
  await page.locator('#username').fill('admin'); await page.locator('#password').fill(process.env.MELISSA_DEMO_PASSWORD);
  await page.getByRole('button', { name: 'Iniciar sesión' }).click(); await page.waitForURL('**/operacion');
  await go('/sedes');
  await page.getByRole('button', { name: 'Crear sede', exact: true }).waitFor({ state: 'visible' });
  await page.locator('#sede-id').fill(sedeId); await page.locator('#sede-nombre').fill(`Sede Melissa ${suffix}`);
  await page.getByRole('button', { name: 'Crear sede', exact: true }).click();
  await page.getByRole('cell', { name: sedeId, exact: true }).waitFor();
  let response = await api.get(`/api/sedes/${sedeId}`, { headers }); assert.equal(response.status(), 200);
  await page.getByRole('button', { name: `Editar sede Sede Melissa ${suffix}`, exact: true }).click();
  await page.locator('#sede-nombre').fill(`Sede editada ${suffix}`);
  await page.getByRole('button', { name: 'Guardar cambios', exact: true }).click();
  await page.getByRole('cell', { name: `Sede editada ${suffix}`, exact: true }).waitFor();
  response = await api.get(`/api/sedes/${sedeId}`, { headers }); assert.equal((await response.json()).nombre, `Sede editada ${suffix}`);
  console.log('OK real: crear, listar y editar sede mediante Angular y PostgreSQL');
  await go('/estacionamientos');
  await page.locator('#zona-sede').selectOption(sedeId); await page.locator('#zona-nombre').fill(zonaNombre); await page.locator('#zona-capacidad').fill('6');
  await page.getByRole('button', { name: 'Crear estacionamiento', exact: true }).click();
  await page.getByRole('cell', { name: zonaNombre, exact: true }).waitFor();
  response = await api.get('/api/estacionamientos', { headers });
  let zona = (await response.json()).find(x => x.nombre === zonaNombre); assert.ok(zona); zonaId = zona.id;
  response = await api.get(`/api/estacionamientos/${zonaId}/accesos`, { headers }); assert.equal((await response.json()).length, 2);
  await page.getByRole('button', { name: `Editar estacionamiento ${zonaNombre}`, exact: true }).click();
  await page.locator('#zona-capacidad').fill('7'); await page.locator('#zona-activo').uncheck();
  await page.getByRole('button', { name: 'Guardar cambios', exact: true }).click();
  await page.getByRole('status').filter({ hasText: 'actualizado correctamente' }).waitFor();
  response = await api.get(`/api/estacionamientos/${zonaId}`, { headers }); zona = await response.json(); assert.equal(zona.capacidad, 7); assert.equal(zona.activo, false);
  await page.screenshot({ path: fileURLToPath(new URL('01-estacionamiento-editado.png', output)), fullPage: true });
  console.log('OK real: crear y editar zona; dos accesos automáticos confirmados en API');
  await go('/sedes');
  await page.getByRole('button', { name: `Eliminar sede Sede editada ${suffix}`, exact: true }).click();
  await page.getByRole('button', { name: 'Confirmar eliminación', exact: true }).click();
  await page.getByRole('alert').filter({ hasText: 'estacionamientos asociados' }).waitFor();
  await page.screenshot({ path: fileURLToPath(new URL('02-sede-con-zona-409.png', output)), fullPage: true });
  console.log('OK real: API rechaza eliminar sede asociada y Angular muestra el conflicto 409');
  await go('/estacionamientos');
  await page.getByRole('button', { name: `Eliminar estacionamiento ${zonaNombre}`, exact: true }).click();
  await page.getByRole('button', { name: 'Confirmar eliminación', exact: true }).click();
  await page.getByRole('status').filter({ hasText: 'eliminado correctamente' }).waitFor();
  response = await api.get(`/api/estacionamientos/${zonaId}`, { headers }); assert.equal(response.status(), 404); zonaId = undefined;
  await go('/sedes');
  await page.getByRole('button', { name: `Eliminar sede Sede editada ${suffix}`, exact: true }).click();
  await page.getByRole('button', { name: 'Confirmar eliminación', exact: true }).click();
  await page.getByRole('status').filter({ hasText: 'eliminado correctamente' }).waitFor();
  response = await api.get(`/api/sedes/${sedeId}`, { headers }); assert.equal(response.status(), 404);
  assert.deepEqual(errors, []);
  console.log('OK real: eliminación confirmada de zona y sede; sin errores de JavaScript');
} finally {
  // Limpieza limitada a los identificadores creados por este recorrido.
  if (zonaId) await api.delete(`/api/estacionamientos/${zonaId}`, { headers });
  await api.delete(`/api/sedes/${sedeId}`, { headers });
  await context.close(); await browser.close(); await api.dispose(); await new Promise(resolve => server.close(resolve));
}
