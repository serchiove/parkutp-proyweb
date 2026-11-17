// Recorrido contra Spring Boot y PostgreSQL. Requiere backend local iniciado.
// Crea una sede/estacionamiento de prueba y conserva sus movimientos como evidencia.
import assert from 'node:assert/strict';
import { createRequire } from 'node:module';
import { readFile, mkdir, writeFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import { crearServidor } from '../serve-ricardo.mjs';
const require = createRequire(import.meta.url);
const { chromium, request } = require(process.env.PLAYWRIGHT_MODULE ?? 'playwright');
const config = await readFile(new URL('../../.env', import.meta.url), 'utf8');
const password = config.split(/\r?\n/).find(s => s.startsWith('DEMO_PASSWORD='))?.slice(14).replace(/^['"]|['"]$/g, '');
assert.ok(password, 'Debe existir DEMO_PASSWORD en .env local.');
const api = await request.newContext({ baseURL: 'http://localhost:8081' });
const login = await api.post('/api/auth/login', { data: { username: 'admin', password } });
assert.equal(login.status(), 200, 'Login real de ADMIN');
const token = (await login.json()).token;
const headers = { Authorization: `Bearer ${token}` };
const suffix = Date.now().toString(36).toUpperCase();
const sedeId = `RIC-${suffix}`;
const sede = await api.post('/api/sedes', { headers, data: { id: sedeId, nombre: `Pruebas Ricardo ${suffix}` } });
assert.equal(sede.status(), 201, 'Crear sede exclusiva de pruebas');
const parkResponse = await api.post('/api/estacionamientos', { headers, data: { sedeId, nombre: `Zona Ricardo ${suffix}`, capacidad: 5, activo: true } });
assert.equal(parkResponse.status(), 201);
const parking = await parkResponse.json();
const output = new URL('../../docs/ricardo/evidencias/api-real/', import.meta.url);
await mkdir(output, { recursive: true });
const server = crearServidor(fileURLToPath(new URL('../dist/ricardo/browser/', import.meta.url)));
await new Promise((resolve, reject) => { server.once('error', reject); server.listen(4200, 'localhost', resolve); });
const browser = await chromium.launch({ channel: 'msedge', headless: true });
const results = [];
const check = text => { results.push(text); console.log('OK:', text); };

async function open(rol, path = 'operacion') {
  const context = await browser.newContext({ viewport: { width: 1440, height: 1050 }, locale: 'es-PE' });
  const page = await context.newPage();
  page.setDefaultTimeout(15000);
  const errors = [];
  page.on('pageerror', error => errors.push(error.message));
  await page.route('https://fonts.googleapis.com/**', route => route.abort());
  await page.goto(`http://localhost:4200/${path}?estacionamiento=${parking.id}`);
  await page.locator('#username').fill(rol);
  await page.locator('#password').fill(password);
  await page.getByRole('button', { name: 'Iniciar sesión' }).click();
  await page.locator('#parking').selectOption(String(parking.id));
  return { context, page, errors };
}
async function capture(page, name) {
  await page.evaluate(() => scrollTo(0, 0));
  await page.screenshot({ path: fileURLToPath(new URL(name, output)), fullPage: true });
}
try {
  const admin = await open('admin', 'accesos');
  const page = admin.page;
  const nav = page.getByRole('navigation', { name: 'Gestión de estacionamientos' });
  await page.getByRole('cell', { name: 'Acceso de ingreso', exact: true }).waitFor();
  check('Login Angular, CORS e interceptor JWT funcionan con la API real.');
  await page.getByRole('row').filter({ hasText: 'Acceso de salida' }).getByRole('button', { name: 'Eliminar', exact: true }).click();
  await page.getByRole('button', { name: 'Confirmar eliminación' }).click();
  await page.getByText('Acceso «Acceso de salida» eliminado correctamente.').waitFor();
  check('DELETE real de un acceso sin movimientos, con confirmación.');
  await page.locator('#nombre').fill('Salida Ricardo');
  await page.locator('#tipo').selectOption('SALIDA');
  await page.getByRole('button', { name: 'Crear acceso', exact: true }).click();
  await page.getByText('Acceso «Salida Ricardo» creado correctamente.').waitFor();
  check('POST real crea y muestra el acceso de salida.');
  await page.getByRole('row').filter({ hasText: 'Acceso de ingreso' }).getByRole('button', { name: 'Editar', exact: true }).click();
  assert.equal(await page.locator('#tipo').isDisabled(), true);
  await page.locator('#nombre').fill('Ingreso Ricardo');
  await page.getByRole('button', { name: 'Guardar cambios' }).click();
  await page.getByText('Acceso «Ingreso Ricardo» actualizado correctamente.').waitFor();
  check('PUT real actualiza nombre y conserva tipo.');
  await capture(page, '01-accesos-crud.png');
  await page.locator('#nombre').fill('Entrada duplicada');
  await page.getByRole('button', { name: 'Crear acceso', exact: true }).click();
  await page.getByRole('alert').filter({ hasText: 'Ya existe un acceso de ese tipo' }).waitFor();
  check('Conflicto real 409 por acceso duplicado visible en Angular.');
  await nav.getByRole('link', { name: 'Operación', exact: true }).click();
  await page.locator('#placa').fill('ric-123');
  await page.getByRole('button', { name: 'Registrar movimiento' }).click();
  await page.getByText(/Ingreso registrado: RIC-123\. Movimiento #/).waitFor();
  await page.getByText('Ocupación: 20 %', { exact: true }).waitFor();
  await capture(page, '02-ingreso-aforo.png');
  check('Ingreso real por Angular: normalización de placa y aforo 1/5.');
  await page.locator('#placa').fill('RIC-123');
  await page.getByRole('button', { name: 'Registrar movimiento' }).click();
  await page.getByRole('alert').filter({ hasText: 'ya está dentro' }).waitFor();
  check('Backend rechaza ingreso duplicado y conserva aforo.');
  await page.locator('#mov-tipo').selectOption('SALIDA');
  await page.getByRole('button', { name: 'Registrar movimiento' }).click();
  await page.getByText(/Salida registrada: RIC-123\. Movimiento #/).waitFor();
  await page.getByText('Ocupación: 0 %', { exact: true }).waitFor();
  await capture(page, '03-salida-aforo.png');
  check('Salida real por Angular: aforo vuelve a 0/5.');
  await page.locator('#placa').fill('RIC-123');
  await page.getByRole('button', { name: 'Registrar movimiento' }).click();
  await page.getByRole('alert').filter({ hasText: 'no tiene una entrada pendiente' }).waitFor();
  check('Backend rechaza salida sin entrada pendiente.');
  await nav.getByRole('link', { name: 'Historial', exact: true }).click();
  await page.getByRole('heading', { name: 'Movimientos (2)', exact: true }).waitFor();
  const today = new Intl.DateTimeFormat('sv-SE', { timeZone: 'America/Lima' }).format(new Date());
  await page.locator('#fecha').fill(today);
  await page.locator('#filtro-placa').fill('ric');
  await page.locator('#filtro-tipo').selectOption('ENTRADA');
  await page.getByRole('button', { name: 'Buscar', exact: true }).click();
  await page.getByRole('heading', { name: 'Movimientos (1)', exact: true }).waitFor();
  await page.getByRole('cell', { name: 'RIC-123', exact: true }).waitFor();
  await capture(page, '04-historial-filtrado.png');
  check('Filtros reales combinados por fecha local, placa parcial y tipo.');
  await nav.getByRole('link', { name: 'Accesos', exact: true }).click();
  await page.getByRole('row').filter({ hasText: 'Ingreso Ricardo' }).getByRole('button', { name: 'Eliminar', exact: true }).click();
  await page.getByRole('button', { name: 'Confirmar eliminación' }).click();
  await page.getByRole('alert').filter({ hasText: 'movimientos o dispositivos asociados' }).waitFor();
  await capture(page, '05-proteccion-historial.png');
  check('Integridad real: un acceso utilizado no se elimina.');
  assert.deepEqual(admin.errors, []);
  await admin.context.close();

  const operador = await open('operador');
  await operador.page.getByRole('button', { name: 'Registrar movimiento' }).click();
  await operador.page.getByText(/Ingreso registrado: sin placa\. Movimiento #/).waitFor();
  await operador.page.locator('#mov-tipo').selectOption('SALIDA');
  await operador.page.getByRole('button', { name: 'Registrar movimiento' }).click();
  await operador.page.getByText(/Salida registrada: sin placa\. Movimiento #/).waitFor();
  check('OPERADOR registra ingreso y salida sin placa con la API real.');
  await operador.page.getByRole('navigation', { name: 'Gestión de estacionamientos' }).getByRole('link', { name: 'Accesos', exact: true }).click();
  await operador.page.getByText('Puedes consultar los accesos.').waitFor();
  assert.equal(await operador.page.getByRole('button', { name: 'Crear acceso', exact: true }).count(), 0);
  assert.deepEqual(operador.errors, []);
  await operador.context.close();

  const consulta = await open('consulta');
  await consulta.page.getByText('Tu rol permite consultar el aforo.').waitFor();
  assert.equal(await consulta.page.getByRole('button', { name: 'Registrar movimiento' }).count(), 0);
  await capture(consulta.page, '06-rol-consulta.png');
  assert.deepEqual(consulta.errors, []);
  await consulta.context.close();
  const consultaLogin = await api.post('/api/auth/login', { data: { username: 'consulta', password } });
  const consultaToken = (await consultaLogin.json()).token;
  const forbidden = await api.post(`/api/estacionamientos/${parking.id}/movimientos`, {
    headers: { Authorization: `Bearer ${consultaToken}` },
    data: { eventoId: crypto.randomUUID(), tipo: 'ENTRADA', origen: 'MANUAL', placa: null },
  });
  assert.equal(forbidden.status(), 403);
  check('CONSULTA: formulario oculto y backend rechaza POST directo con 403.');

  const historial = await api.get(`/api/estacionamientos/${parking.id}/movimientos`, { headers });
  const movimientos = await historial.json();
  assert.equal(movimientos.length, 4);
  const aforo = await api.get(`/api/estacionamientos/${parking.id}`, { headers });
  assert.equal((await aforo.json()).ocupados, 0);
  check('Consulta final real: 4 movimientos guardados y aforo 0.');
  await writeFile(new URL('resultados.json', output), JSON.stringify({
    fecha: new Date().toISOString(), entorno: 'Angular + Spring Boot + PostgreSQL 18.7 local; API sin simulación',
    sedeId, estacionamientoId: parking.id, nombreEstacionamiento: parking.nombre,
    movimientos: movimientos.map(({ id, tipo, placa, registradoEn }) => ({ id, tipo, placa, registradoEn })),
    resultados: results,
  }, null, 2));
  console.log(`${results.length} comprobaciones reales aprobadas. Estacionamiento de prueba #${parking.id}.`);
} finally {
  await browser.close(); server.closeAllConnections(); server.close(); await api.dispose();
}
