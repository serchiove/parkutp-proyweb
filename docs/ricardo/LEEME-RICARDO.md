# Entrega de Ricardo: accesos, operación e historial

Fecha: 17 de noviembre de 2026.

## Qué se implementó

Se añadieron tres pantallas Angular conectadas a los endpoints REST que ya trae el backend. Todo el código del módulo está en `frontend/src/app/features/gestion-estacionamientos/`. El aporte conserva autenticación, sedes, estacionamientos, estilos globales, backend y dependencias. La integración posterior conecta las tres pantallas en las rutas principales y agrega Accesos al menú. La rama parte de la actualización de diseño del coordinador (ea613e4) y reutiliza su tipografía mediante las variables existentes.

Las rutas de accesos, operación e historial ya están incorporadas en el AppShell protegido. Se pueden abrir con el arranque normal `npm start`. La entrada de revisión independiente se conserva como herramienta opcional.

## Cómo abrirlo y probarlo contra la API real

En esta laptop ya se comprobó PostgreSQL 18.7 y quedó preparada la base de demostración. Para futuros arranques, desde la raíz `parkutp-proyweb`, ejecuta:

```powershell
.\docs\ricardo\Iniciar-PruebaLocal.ps1
```

El script carga las claves del `.env` local (excluido de Git) y llama al iniciador original con `-DemoLocal`. No necesitas la contraseña del usuario `postgres` de la instalación principal: esta base independiente usa el puerto 55432 y datos dentro de `parkutp/.local-data`. Usuarios de la web: `admin`, `operador`, `consulta`; la contraseña de prueba está en el valor `DEMO_PASSWORD` del `.env`. No cambiar ese valor para intentar actualizar una contraseña de cuentas ya creadas.

Deja esa terminal abierta y, en otra terminal dentro de `frontend`, ejecuta `npm start`. Ambos procesos deben estar encendidos. Con Ctrl+C detienes cada proceso; la base local puede detenerse con `parkutp/scripts/Detener-PostgresLocal.ps1`.

1. Levanta el backend y PostgreSQL siguiendo el README original o `parkutp/docs/Docker.md`. La URL esperada por el frontend existente es `http://localhost:8081/api`. Usa las credenciales que configuró el equipo; esta entrega no cambia ni agrega usuarios o contraseñas.
2. Abre una terminal en `frontend`.
3. Instala las dependencias originales con `npm ci` si aún no están instaladas.
4. Ejecuta:

```powershell
npm start
```

5. Abre `http://localhost:4200/operacion`. Si no tienes sesión, se muestra el login original. Usa `localhost`, porque es el origen autorizado por defecto en el backend.
6. Navega entre Accesos, Operación e Historial mediante las pestañas dentro de la pantalla. La barra lateral del coordinador se conserva.

El puerto 4200 debe estar libre; detén primero cualquier `ng serve` que use ese puerto. El servidor de desarrollo de Angular recarga los cambios automáticamente mientras está abierto.

## Explicación de cada implementación

### 1. Modelos y servicio de API

`data/parking.models.ts` define la forma de un estacionamiento, acceso, movimiento y filtros. TypeScript puede detectar datos o nombres de campos incorrectos antes de ejecutar la aplicación.

`data/parking-api.service.ts` reúne las llamadas HTTP:

| Acción | Solicitud |
| --- | --- |
| Cargar selector | `GET /api/estacionamientos` |
| Actualizar aforo | `GET /api/estacionamientos/{id}` |
| Listar accesos | `GET /api/estacionamientos/{id}/accesos` |
| Crear acceso | `POST /api/estacionamientos/{id}/accesos` |
| Editar acceso | `PUT /api/estacionamientos/{id}/accesos/{accesoId}` |
| Eliminar acceso | `DELETE /api/estacionamientos/{id}/accesos/{accesoId}` |
| Registrar ingreso o salida | `POST /api/estacionamientos/{id}/movimientos` |
| Consultar historial | `GET /api/estacionamientos/{id}/movimientos` con filtros opcionales |

El servicio usa el `HttpClient` de Angular y la constante `API_BASE_URL` existente. No agrega el token manualmente: el interceptor del coordinador añade `Authorization: Bearer …`. El login y su `AuthService` siguen siendo los originales.

### 2. Selector y estado del estacionamiento

`shared/parking-context.ts` guarda la lista y el estacionamiento seleccionado dentro de cada pantalla. También calcula disponibles, porcentaje y permisos a partir del rol que entrega `AuthService`.

`shared/parking-selector.ts` muestra el selector y las pestañas. Al navegar pasa el ID mediante `?estacionamiento=1`, para conservar la selección entre los módulos. Incluye estados de carga, error, lista vacía y actualización manual.

Estos componentes solo leen estacionamientos existentes. No implementan ni modifican el CRUD que corresponde a Melissa.

### 3. Accesos

`accesos/accesos-page.ts` contiene la lógica y el formulario reactivo; `accesos-page.html` contiene la pantalla.

- Lista accesos por estacionamiento.
- Crea un acceso con nombre obligatorio de hasta 80 caracteres y tipo ENTRADA o SALIDA.
- Recorta espacios del nombre y rechaza nombres que solo contienen espacios.
- Permite editar el nombre. El selector de tipo se bloquea al editar porque el backend no permite cambiarlo.
- Pide una confirmación dentro de la pantalla antes de eliminar.
- Presenta confirmaciones de éxito y errores de la API, por ejemplo tipo duplicado o acceso con historial asociado.
- Solo muestra acciones de modificación para ADMIN. OPERADOR y CONSULTA pueden consultar.
- Bloquea envíos repetidos mientras se guarda y actualiza la lista al terminar.

La API admite un único acceso de cada tipo por estacionamiento. Además, al crear estacionamientos el backend ya genera sus accesos: que aparezcan accesos desde el inicio es el comportamiento esperado.

### 4. Operación

`operacion/operacion-page.ts` gestiona el registro; `operacion-page.html` muestra aforo y formulario.

- Presenta capacidad, ocupados, disponibles, porcentaje y barra de ocupación.
- Registra ENTRADA o SALIDA con origen MANUAL.
- La placa es opcional. Si se escribe, debe tener de 3 a 12 letras, números o guiones; se envía en mayúsculas y sin espacios exteriores.
- Actualiza el aforo después de cada registro confirmado y permite actualizarlo manualmente. No hay actualización periódica automática.
- Muestra el motivo de rechazo que devuelva el backend: placa dentro, salida sin entrada, falta de acceso, estacionamiento lleno o inactivo.
- ADMIN y OPERADOR pueden registrar; CONSULTA ve el aforo.
- El backend elige el acceso según el tipo de movimiento. No se envía un `accesoId` inventado ni se implementa una regla diferente en Angular.

Cada nuevo movimiento lleva un `eventoId` generado mediante `crypto.randomUUID()`. Si la respuesta se pierde, podría haberse guardado en el servidor: generar otro UUID en ese momento podría duplicar un movimiento sin placa. Por eso se conserva el envío y se ofrece reintentar el mismo evento. El backend ya soporta esta idempotencia.

`shared/pending-movement.ts` guarda ese envío incierto en `sessionStorage`, separado por usuario, para recuperarlo al volver a la pantalla o recargar la pestaña. Guarda ID de estacionamiento, evento, tipo, origen y placa; no guarda contraseñas ni tokens. Se elimina al confirmar el registro o recibir un rechazo HTTP 4xx. Si el navegador bloquea el almacenamiento, solo se conserva durante la vida de la pantalla. Cerrar la pestaña elimina el almacenamiento de sesión; antes de repetir manualmente un registro incierto, revisa el historial.

Si el registro termina correctamente pero falla la consulta de aforo, se muestra el éxito del registro y un aviso separado. No se invita a registrar nuevamente un movimiento ya confirmado.

### 5. Historial

`historial/historial-page.ts` gestiona la búsqueda; `historial-page.html` muestra filtros y resultados.

- Combina fecha, placa completa o parcial y tipo.
- Envía la fecha como `AAAA-MM-DD`; el backend interpreta el día de Perú.
- Normaliza el filtro de placa a mayúsculas.
- Presenta la fecha y hora del resultado en `America/Lima`.
- Actualizar conserva los últimos filtros aplicados; Limpiar filtros consulta sin filtros.
- Presenta carga, error y búsqueda sin resultados.
- Advierte cuando se alcanzan 50 resultados, que es el límite existente de la API. No agrega una paginación inexistente.
- Descarta respuestas viejas cuando se cambia de estacionamiento durante una consulta, para evitar mostrar movimientos de otra selección.

### 6. Errores y estilos

`shared/api-error.ts` convierte errores de conexión, 401, 403, 404 y mensajes de validación o negocio en texto para el usuario. Los errores 401 siguen siendo atendidos por el interceptor original, que redirige al login.

`shared/gestion-estacionamientos.scss` contiene estilos limitados a estos componentes y reutiliza los colores y variables del proyecto. Incluye formularios adaptables y tablas con desplazamiento local para pantallas estrechas.

## Ejemplo del recorrido de un dato

Al registrar `abc-123` como ingreso:

1. El formulario valida la placa y la convierte en `ABC-123`.
2. Se genera un UUID y se construye `{ eventoId, tipo: 'ENTRADA', origen: 'MANUAL', placa: 'ABC-123' }`.
3. `ParkingApiService` envía el POST al estacionamiento seleccionado.
4. El interceptor existente añade el JWT.
5. El backend comprueba permisos, acceso, aforo y estado de la placa, y guarda el movimiento.
6. Angular muestra el ID devuelto y consulta de nuevo el aforo.
7. En Historial, una búsqueda por `ABC` permite encontrarlo.

## Validación realizada y límites

La integración posterior aprobó 25 comprobaciones de navegador con API simulada usando la compilación normal de producción, incluida la navegación desde las tres opciones del menú principal. El reporte y una captura del menú integrado están en `evidencias/integracion-rutas/`. Las compilaciones normales de desarrollo y producción también pasaron.

- Compilación Angular de desarrollo de las tres pantallas: aprobada.
- Compilación de producción: aprobada, con advertencias de tamaño en los estilos originales de login y app-shell.
- 24 comprobaciones de navegador con Microsoft Edge y API simulada: aprobadas. Incluyen CRUD de accesos, errores 409, validación de placa, ingresos y salidas, UUID y reintentos tras navegar, aforo, filtros, respuestas tardías, permisos, conexión, sesión vencida y ancho móvil.
- Capturas y resultados: `evidencias/frontend-simulado/`. Son capturas reales del frontend ejecutándose con respuestas simuladas, no evidencia de persistencia ni de un recorrido contra PostgreSQL.
- Recorrido real completado con Angular, Spring Boot y PostgreSQL 18.7: 14 comprobaciones aprobadas. Se comprobaron CRUD de accesos, errores reales, ingreso/salida con placa y sin placa, aforo, filtros y permisos. Capturas y resultados en `evidencias/api-real/`.
- Persistencia comprobada tras reiniciar backend y base local: se conservaron los mismos cuatro movimientos y aforo cero; el historial de Angular los mostró correctamente.
- La compilación inicial del backend ejecutó 15 pruebas aprobadas y omitió 57 pruebas que requieren la base dedicada `TEST_DB_URL`. El recorrido real anterior se ejecutó por separado en la base de demostración; no equivale a ejecutar esas 57 pruebas.
- El aporte se prepara en la rama `ricardo/accesos-operacion-historial` del repositorio del equipo, sobre `origin/main`. El PR #27 incorporó el módulo. La rama ricardo/integracion-rutas-menu propone conectar sus rutas y agregar Accesos al menú para revisión de Sergio.

## Repetir las pruebas automatizadas

Desde `frontend`, después de instalar las dependencias:

```powershell
node node_modules/@angular/cli/bin/ng.js build --configuration development --browser src/main.ricardo.ts --output-path dist/ricardo
```

La prueba requiere Playwright y Microsoft Edge. Si tu equipo aún no tiene Playwright, puedes instalarlo temporalmente sin modificar el manifiesto ni el lockfile:

```powershell
npm install --no-save --package-lock=false playwright
node tests/ricardo.e2e.mjs
```

El script abre su propio servidor en un puerto libre y lo cierra al terminar; no necesita ejecutar `preview-ricardo.mjs` ni levantar el backend. Regenera las capturas simuladas y `resultados.json`. El código de producción no usa Playwright ni datos simulados.

## Orden sugerido para estudiar el código

1. Modelos: qué datos recibe y envía cada pantalla.
2. Servicio API: qué endpoint corresponde a cada acción.
3. Accesos: un CRUD completo con formulario y mensajes.
4. Operación: registro, UUID, aforo y reintento.
5. Historial: filtros y protección frente a respuestas tardías.
6. Componentes compartidos y guía de integración.

Consulta `INTEGRACION-Y-PR.md` para incorporar los archivos al repositorio actualizado y proponer la integración al coordinador.

Las pruebas reales se pueden repetir con `node tests/ricardo.real.e2e.mjs` una vez iniciado el backend y con el puerto 4200 libre. Crea datos nuevos y deja evidencia. Después de reiniciar el backend, `node tests/ricardo.persistence.mjs` compara los mismos IDs y guarda una captura. Ambos scripts requieren Playwright y Edge, y leen las credenciales locales sin incorporarlas al reporte.
