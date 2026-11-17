# Módulos Angular de sedes y estacionamientos

Se sustituyen las dos pantallas pendientes de `/sedes` y `/estacionamientos` por CRUD conectados a la API existente. Se mantienen las URLs, el diseño del AppShell, el AuthService, el interceptor JWT y los módulos de accesos, operación e historial.

## Implementación

- `frontend/src/app/features/administracion/data/administracion-api.service.ts`: contratos y llamadas GET/POST/PUT/DELETE de ambos recursos. La URL base se obtiene de la configuración existente.
- `sedes/sedes-page.*`: listado, alta, edición de nombre conservando el código, confirmación y eliminación.
- `estacionamientos/estacionamientos-page.*`: selección de sede, listado, alta, edición de capacidad y estado, confirmación y eliminación.
- `shared/crud-page.ts`: estado de carga, protección ante envíos repetidos, autorización visual y mensajes de la API. Un fallo de guardado conserva el formulario; un fallo de recarga permite reintentar sin presentar información antigua como actual.
- `shared/administracion.scss`: reutiliza los estilos y variables actuales; no redefine el diseño general.

Solo ADMIN dispone de formularios y acciones de modificación. Se conserva la visibilidad por rol del menú de Sergio. Al navegar directamente, OPERADOR y CONSULTA ven únicamente consulta; el backend conserva la autorización real. Los errores 401 siguen pasando por el interceptor que limpia la sesión y devuelve al login.

Las validaciones coinciden con los DTO: código de sede de 1 a 64 letras/números/guion/guion bajo; nombres no vacíos hasta 120 caracteres; sede obligatoria y capacidad entera entre 1 y 10000. Las restricciones de negocio (historial, sede asociada, duplicados, ocupación y dispositivos) las valida la API y se presenta su mensaje. No se modifica ni se borra historial desde Angular.

## Ejecutar

Con Node y npm instalados, desde `frontend`:

```powershell
npm ci
npm start
```

Iniciar también el backend siguiendo el README de la raíz. La configuración actual apunta a `http://localhost:8081/api`; el frontend de desarrollo usa `http://localhost:4200`. Iniciar sesión con las cuentas existentes. No se agregan contraseñas al código.

Para compilar: `npm run build`.

## Comprobaciones

La compilación de producción está verificada. Angular informa advertencias de tamaño de estilos; no impiden generar la aplicación. Se mantiene el presupuesto actual.

`frontend/tests/melissa.e2e.mjs` recorre el frontend con API simulada: 13 comprobaciones aprobadas de JWT, CRUD, formularios inválidos, capacidad decimal/fuera de rango, confirmación/cancelación, conflictos 409, fallo de carga y recuperación, rechazo 403, expiración 401 y restricción de OPERADOR/CONSULTA. Las imágenes de `evidencias/frontend-simulado` pertenecen a este recorrido y no acreditan persistencia PostgreSQL.

Para repetirlo, instalar Playwright en un entorno de pruebas y sus navegadores. Compilar primero con `npm run build`, ya que el recorrido sirve los archivos de `dist/frontend/browser`. Desde `frontend`:

```powershell
node tests/melissa.e2e.mjs
```

Si se usa Edge instalado: definir `PLAYWRIGHT_CHANNEL=msedge`. Si Playwright está fuera de las dependencias del proyecto, `PLAYWRIGHT_MODULE` acepta la ruta a su paquete. No se cambió package.json ni package-lock.json para introducir herramientas de pruebas.

`frontend/tests/melissa.real.e2e.mjs` permite revisar contra la API real. Requiere una base de revisión separada, backend en 8081, CORS_ORIGENES con `http://127.0.0.1:5310` y `MELISSA_DEMO_PASSWORD` configurada localmente. Crea registros identificados por `MEL-...`, verifica por API los cambios y elimina solo esos registros al terminar. No utilizarlo como prueba masiva sobre datos del equipo.

### Ejecución contra PostgreSQL del 17 de noviembre de 2026

Recorrido real aprobado con Java 21, Spring Boot y PostgreSQL 18. Se utilizó exclusivamente `parkutp_angular` en una instancia de revisión en `127.0.0.1:55441`; los datos de `.local-data` de la demo no se utilizaron. Se inició el WAR en 8081 y el frontend compilado en 5310. Al terminar se eliminaron los registros creados por el recorrido y se detuvieron el servidor de revisión y PostgreSQL.

- Crear y editar una sede desde Angular, verificando el registro por la API real: correcto.
- Crear y editar una zona, comprobar capacidad y estado guardados y los dos accesos automáticos: correcto.
- Intentar borrar la sede asociada y mostrar el mensaje de conflicto 409: correcto.
- Confirmar la eliminación de la zona sin historial y luego la sede; comprobar 404 al consultarlas: correcto.
- Ningún error de JavaScript durante el recorrido.

Capturas reales: `evidencias/api-real/01-estacionamiento-editado.png` y `evidencias/api-real/02-sede-con-zona-409.png`.

El WAR se empaquetó con `-DskipTests` para este recorrido. No se afirma una nueva ejecución de las 72 pruebas Java del APF2; este cambio se verificó mediante compilación Angular, pruebas del frontend y recorrido contra API real.

## Recorrido para la presentación

1. ADMIN: crear sede y modificar su nombre; mostrar que el código no se cambia.
2. Crear zona en esa sede y editar su capacidad/estado. Los dos accesos los crea el backend existente.
3. Intentar eliminar la sede asociada: mostrar el conflicto y conservar el formulario/listado.
4. Eliminar la zona sin historial y después la sede, confirmando cada operación.
5. Mostrar validaciones de código/nombre/capacidad y el tratamiento de errores.
6. Si se requiere acreditar persistencia, usar la base de revisión, reiniciar y volver a consultar; no confundir el recorrido simulado con esa evidencia.

Los módulos se entregan en la rama Melissa. Las pruebas y esta documentación se mantienen en un commit separado de la implementación.
