# Integración de accesos, operación e historial

Las tres pantallas ya se abren desde las rutas normales de Angular:

- `/accesos`
- `/operacion`
- `/historial`

`app.routes.ts` incorpora `gestionEstacionamientoRoutes` dentro del AppShell protegido por `authGuard`. Se retiraron las rutas provisionales de operación e historial para evitar que Angular muestre el placeholder. El menú lateral ahora incluye Accesos.

Las rutas de sedes y estacionamientos, el login y la redirección inicial se mantienen. La entrada `main.ricardo.ts` sigue disponible para revisión aislada, pero ya no es necesaria para usar los módulos.

## Ejecutar

Inicia el backend con la configuración local del equipo. Después, desde `frontend`:

```powershell
npm start
```

Abre `http://localhost:4200/operacion`, inicia sesión y navega desde la barra lateral. Si hay otro servidor en el puerto 4200, detén esa terminal con Ctrl+C antes de arrancar Angular.

## Comprobar la integración

Desde `frontend`, compila la entrada normal:

```powershell
npm run build -- --configuration development
```

Para ejecutar las pruebas de navegador sobre ese resultado (requiere Playwright y Edge):

```powershell
$env:BUILD_DIR = '../dist/frontend/browser/'
$env:EVIDENCE_DIR = '../.angular/evidencias-integracion/'
node tests/ricardo.e2e.mjs
```

Las rutas de estas dos variables son relativas al archivo de pruebas. La prueba inicia su propio servidor y simula la API; no necesita detener el backend. Además del recorrido del módulo, comprueba que las tres opciones del menú principal abren las pantallas correspondientes.

Las pruebas previas contra PostgreSQL y las capturas reales siguen en `evidencias/api-real/`. La integración no cambia los endpoints ni las reglas de negocio.

## Revisión para Sergio

Este ajuste termina de conectar las pantallas del PR #27 al arranque normal del proyecto. Se propone en una rama y un PR nuevos, para que Sergio pueda revisar por separado los cambios en rutas y menú antes de fusionarlos.
