# Frontend Angular de ParkUTP

Interfaz del avance 3 conectada a la API REST de Spring Boot. Para preparar Java, PostgreSQL y las cuentas locales, sigue el [README principal](../README.md#ejecutar-en-windows).

## Arranque

Desde esta carpeta, con una versión de Node compatible con Angular CLI 22.2.7 y npm disponibles:

```powershell
npm ci
npm start
```

Abre `http://localhost:4200/login`. La API debe estar iniciada en otra terminal en `http://localhost:8081/api`. El arranque de Angular no inicia el backend.

## Pantallas

| Ruta | Módulo |
|---|---|
| `/login` | Inicio de sesión. |
| `/sedes` | Sedes. |
| `/estacionamientos` | Estacionamientos. |
| `/accesos` | Accesos por estacionamiento. |
| `/operacion` | Aforo y movimientos según el rol. |
| `/historial` | Movimientos con filtros. |

La navegación comparte `AppShell`. `AuthService` conserva la sesión en memoria y el interceptor añade el JWT a las solicitudes. Al recargar se requiere iniciar sesión de nuevo. Las opciones y los formularios respetan el rol; la API verifica los permisos efectivos.

## Organización del código

- `src/app/core/`: autenticación, guard, interceptor y configuración de API.
- `src/app/layout/`: estructura y menú compartidos.
- `src/app/features/administracion/`: sedes, estacionamientos, formularios y aviso de cambios sin guardar.
- `src/app/features/gestion-estacionamientos/`: accesos, operación e historial.
- `tests/`: recorridos con API simulada y real.

La URL se configura en [api-config.ts](src/app/core/api/api-config.ts). Si cambia el backend, ajustar esa URL y el origen CORS permitido en el servidor.

## Compilación

```powershell
npm run build
```

La aplicación integrada se genera en `dist/frontend/browser/`. `npm run watch` compila en modo de desarrollo y observa los cambios. `npm start` ejecuta el servidor de desarrollo con recarga automática.

## Pruebas

Los recorridos de `tests/` usan Playwright preparado en un entorno de pruebas; no se instala mediante el `npm ci` del proyecto. Compilar antes de ejecutar los recorridos que sirven `dist/frontend/browser/`. Cada prueba real tiene requisitos propios de puerto, credenciales, datos y salida compilada.

- [Sedes, estacionamientos y formularios](../docs/melissa/LEEME-MELISSA.md).
- [Accesos, operación, historial y persistencia](../docs/ricardo/LEEME-RICARDO.md).

Aunque `package.json` conserva un script `test`, el `angular.json` actual no configura un target de pruebas unitarias. No se presenta `npm test` ni `ng e2e` como procedimientos configurados de este proyecto.

Los recorridos con API simulada verifican la interfaz; no demuestran persistencia PostgreSQL. Los recorridos con API real pueden crear o conservar registros de prueba: leer la guía correspondiente antes de ejecutarlos.
