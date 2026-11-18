# ParkUTP: frontend Angular

Interfaz del avance 3 integrada con Spring Boot y PostgreSQL. Verificada con Node 24 y Microsoft Edge en Windows.

## Inicio

Con el backend disponible en http://localhost:8081, ejecutar desde frontend:

```powershell
npm.cmd ci
npm.cmd start
```

Abrir http://localhost:4200. ADMIN administra sedes, estacionamientos y accesos; OPERADOR registra movimientos; CONSULTA consulta aforo e historial. El backend valida los permisos. La sesión se conserva en `sessionStorage` durante las recargas de esa pestaña; al cerrar sesión o recibir un 401 se elimina.

## Verificación

```powershell
npm.cmd test
```

Compila y ejecuta los recorridos simulados, incluido el caso de mantener la sesión tras recargar. Estas pruebas no acreditan persistencia. Playwright se instala con npm ci. En Windows se utiliza Edge; para otro navegador configurar PLAYWRIGHT_CHANNEL. En Linux instalar Chromium con `npx playwright install chromium`.

Las pruebas reales requieren el backend, una cuenta demo y datos de prueba. `npm.cmd run test:api` admite DEMO_PASSWORD en el entorno o en el archivo local .env. Crea una sede identificada con RIC- y conserva cuatro movimientos para revisar persistencia. Nunca imprime la contraseña.

`npm.cmd run test:sedes:api` requiere MELISSA_DEMO_PASSWORD; crea y elimina exclusivamente sus propios registros. Su frontend usa 127.0.0.1:5310 por defecto. Para la configuración CORS local de Docker:

```powershell
$env:TEST_FRONTEND_HOST = 'localhost'
$env:TEST_FRONTEND_PORT = '4200'
npm.cmd run test:sedes:api
```

No ejecutar dos servidores en el mismo puerto. Los recorridos actualizan imágenes bajo docs. Revisar sus diferencias antes de hacer commit.

## Compilación

`npm.cmd run build` genera dist/frontend/browser. Todas las suites utilizan esa salida. Las advertencias del presupuesto CSS se documentan en el cierre APF3.
