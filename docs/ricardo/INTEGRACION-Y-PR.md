# Integración de Accesos, Operación e Historial

## Estado actual

Las tres pantallas de Ricardo están conectadas al frontend principal:

| Ruta | Pantalla |
| --- | --- |
| `/accesos` | Gestión de accesos por estacionamiento |
| `/operacion` | Registro de ingresos y salidas, y consulta de aforo |
| `/historial` | Historial con filtros por fecha, placa y tipo |

Las rutas se montan dentro de `AppShell`, que conserva la protección del `authGuard`. Se retiraron los placeholders de Operación e Historial y se añadió Accesos al menú lateral. Las rutas provisionales de Sedes y Estacionamientos se mantienen sin cambios.

## Archivos de integración

- `frontend/src/app/app.routes.ts`: importa `gestionEstacionamientoRoutes` dentro de las rutas protegidas.
- `frontend/src/app/features/gestion-estacionamientos/gestion-estacionamientos.routes.ts`: declara las tres rutas con carga diferida.
- `frontend/src/app/layout/app-shell/app-shell.ts`: incorpora Accesos a la navegación.

Las pantallas reutilizan `AuthService`, `HttpClient`, el interceptor JWT y `API_BASE_URL`. No se cambió el backend ni el flujo de login.

## Cómo ejecutar

Desde `frontend`, inicia el frontend integrado con:

```powershell
npm start
```

La API debe estar disponible en `http://localhost:8081/api`. `preview-ricardo.mjs` sigue disponible como entrada aislada de revisión, pero ya no hace falta para navegar las rutas desde la aplicación principal.

## Pruebas y evidencias

Los scripts de navegador están en `frontend/tests/ricardo.*.mjs`. Las capturas y reportes se guardan en `docs/ricardo/evidencias/`, organizados en `api-real/` y `frontend-simulado/`.

La entrega de Ricardo reporta compilaciones aprobadas, 24 comprobaciones con API simulada y 14 con API real. También reporta la persistencia de cuatro movimientos después de reiniciar el backend y PostgreSQL. Estos resultados corresponden a las pruebas documentadas por Ricardo; esta nota solo registra la integración de rutas y navegación.

La configuración local permanece en `.env` y no debe subirse. No se deben incluir contraseñas, tokens completos ni datos personales en evidencias.
