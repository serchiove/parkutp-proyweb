// Ejecutar DESPUÉS de reiniciar el backend y de ricardo.real.e2e.mjs.
import assert from 'node:assert/strict';
import { readFile, writeFile } from 'node:fs/promises';
import { createRequire } from 'node:module';
import { fileURLToPath } from 'node:url';
import { crearServidor } from '../serve-ricardo.mjs';
const { chromium, request } = createRequire(import.meta.url)(process.env.PLAYWRIGHT_MODULE ?? 'playwright');
const output = new URL('../../docs/ricardo/evidencias/api-real/', import.meta.url);
const report = JSON.parse(await readFile(new URL('resultados.json', output), 'utf8'));
const config = await readFile(new URL('../../.env', import.meta.url), 'utf8');
const password = config.split(/\r?\n/).find(s => s.startsWith('DEMO_PASSWORD='))?.slice(14).replace(/^['"]|['"]$/g, '');
const api = await request.newContext({ baseURL: 'http://localhost:8081' });
const login = await api.post('/api/auth/login', { data: { username: 'admin', password } });
assert.equal(login.status(), 200);
const headers = { Authorization: `Bearer ${(await login.json()).token}` };
const movimientosResponse = await api.get(`/api/estacionamientos/${report.estacionamientoId}/movimientos`, { headers });
assert.equal(movimientosResponse.status(), 200);
const movimientos = await movimientosResponse.json();
assert.deepEqual(movimientos.map(m => m.id).sort(), report.movimientos.map(m => m.id).sort());
assert.equal(movimientos.length, 4);
const parking = await api.get(`/api/estacionamientos/${report.estacionamientoId}`, { headers });
assert.equal((await parking.json()).ocupados, 0);
const server = crearServidor(fileURLToPath(new URL('../dist/frontend/browser/', import.meta.url)));
await new Promise((resolve, reject) => { server.once('error', reject); server.listen(4200, 'localhost', resolve); });
const browser = await chromium.launch({ channel: process.env.PLAYWRIGHT_CHANNEL ?? (process.platform === 'win32' ? 'msedge' : undefined), headless: true });
try {
  const page = await browser.newPage({ viewport: { width: 1440, height: 1050 } });
  await page.route('https://fonts.googleapis.com/**', route => route.abort());
  await page.goto(`http://localhost:4200/historial?estacionamiento=${report.estacionamientoId}`);
  await page.locator('#username').fill('admin');
  await page.locator('#password').fill(password);
  await page.getByRole('button', { name: 'Iniciar sesión' }).click();
  await page.locator('.count-badge').filter({ hasText: /^4$/ }).waitFor();
  await page.evaluate(() => scrollTo(0, 0));
  await page.screenshot({ path: fileURLToPath(new URL('07-despues-reinicio.png', output)), fullPage: true });
  report.persistencia = { fecha: new Date().toISOString(), resultado: 'Después de reiniciar el backend: mismos 4 IDs de movimiento, aforo 0 y consulta Angular correcta.' };
  await writeFile(new URL('resultados.json', output), JSON.stringify(report, null, 2));
  console.log('OK: persistencia después del reinicio; mismos 4 movimientos, aforo 0 y captura Angular guardada.');
} finally {
  await browser.close(); server.closeAllConnections(); server.close(); await api.dispose();
}
