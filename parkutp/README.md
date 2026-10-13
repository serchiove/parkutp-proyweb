# ParkUTP — Avance 2

Evolución del proyecto APF1 para gestionar los estacionamientos de la UTP Arequipa. Conserva la API de sedes y añade persistencia PostgreSQL mediante JPA para sedes, estacionamientos y accesos, CRUD de estacionamientos, interfaz JSP/Bootstrap y un formulario JSF de sedes. Dos accesos separados: uno de entrada y otro de salida.

## Ejecutar en Windows

Requisitos: JDK 21, PostgreSQL instalado y acceso a Maven Central en la primera compilación. Bootstrap se incluye en el proyecto mediante WebJars: las pantallas no dependen de un CDN.

Desde esta carpeta, con PostgreSQL instalado y el puerto 55432 libre:

```powershell
$env:DEMO_PASSWORD = [System.Net.NetworkCredential]::new('', (Read-Host 'Clave de cuentas demo' -AsSecureString)).Password
.\scripts\Iniciar.ps1 -DemoLocal
```

El script crea una instancia PostgreSQL independiente, enlazada a 127.0.0.1, y conserva los datos en `.local-data`. Usa autenticación local de confianza: es solo para una demostración en esta computadora. No configura ni modifica el servicio PostgreSQL existente. Al terminar, Ctrl+C detiene la aplicación; para detener esa instancia de base de datos:

```powershell
.\scripts\Detener-PostgresLocal.ps1
```

Alternativa con una base existente dedicada al proyecto:

```powershell
$env:JAVA_HOME = 'C:\ruta\al\jdk-21'
$env:DB_URL = 'jdbc:postgresql://localhost:5432/parkutp'
$env:DB_USER = 'parkutp'
$env:DB_PASSWORD = 'tu-clave'
$env:DEMO_PASSWORD = [System.Net.NetworkCredential]::new('', (Read-Host 'Clave de cuentas demo' -AsSecureString)).Password
.\scripts\Iniciar.ps1
```

Crear antes la base `parkutp` y un usuario con permisos sobre ella; la aplicación crea las tablas si no existen. No guardar contraseñas en Git. `schema.sql` sirve para una instalación inicial; cambios futuros del esquema necesitarán migraciones.

Iniciar sesión con `admin`, `operador` o `consulta` y la clave indicada en DEMO_PASSWORD; las cuentas existentes no se sobrescriben. Abrir [Panel](http://localhost:8081/estacionamientos) y [Sedes JSF](http://localhost:8081/jsf/sedes.xhtml). Registrar primero una sede (ejemplo `AQP-PARRA` / `UTP Arequipa - Sede Parra`) y luego un estacionamiento con capacidad 20. No se incluyen capacidades reales ni datos institucionales inventados.

## Arquitectura y distribución

```text
controller/web    MVC: formularios JSP y navegación
controller/api    API REST y errores HTTP
security          Roles, JWT, BCrypt, sesión web y autorización
view              SedeBean: formulario JSF administrado por Spring/JoinFaces
dto               Datos de entrada y validaciones
model             Sede, Estacionamiento, Movimiento
service           Reglas de negocio y límites transaccionales
dao               Adaptadores JPA para sedes, estacionamientos y movimientos
entity            Sede, Estacionamiento, Acceso, Dispositivo y Movimiento mapeados con JPA
repository        CRUD Spring Data JPA y consultas JPQL de historial y aforo
src/main/webapp   JSP en WEB-INF y Facelet sedes.xhtml
resources         Esquema PostgreSQL, estilos y JS de operación
```

No hay SQL en los controllers, JSP ni el bean JSF. Los CRUD de sede, estacionamiento y acceso y el registro de movimientos usan JPA/Hibernate. El historial y aforo se consultan por JPQL. PostgreSQL genera la fecha de cada movimiento y Hibernate la recupera para la respuesta. El WAR permite ejecutar JSP con Tomcat embebido. JoinFaces integra Jakarta Faces 4 con Spring Boot 3.2.5. El término Managed Bean se implementa como bean administrado por Spring, visible en EL de JSF; no se usa la anotación histórica `javax.faces.bean.ManagedBean` de versiones antiguas.

## Adecuación al checklist APF2

Primera etapa implementada: entidad de movimientos, repositorio de lectura y consulta JPQL del historial con filtros antes del límite de 50 resultados. Hibernate no modifica el esquema existente. Detalles y verificación en [Migración del historial a JPQL](docs/JPQL-historial.md).

El CRUD JPA y la creación transaccional de estacionamiento y accesos están implementados. Detalles y pruebas del aporte de Melissa en [Persistencia JPA](docs/Persistencia-JPA-Melissa.md). El registro transaccional de movimientos también usa JPA; detalles en [Registro de movimientos](docs/Registro-movimientos-JPA.md). Spring Security protege la web y la API con los roles ADMIN, OPERADOR y CONSULTA, login, sesiones, JWT y pruebas de autorización. Configuración y guion en [Seguridad APF2](docs/Seguridad-APF2.md).

## Funciones actuales

| Requisito | Implementación |
|---|---|
| CRUD de entidad principal | Estacionamiento: crear, listar, editar, eliminar desde JSP y REST |
| MVC, DAO, DTO | Paquetes separados, Service como capa adicional de negocio |
| Persistencia JPA | PostgreSQL + Hibernate + repositorios para CRUD y registro de movimientos |
| Validaciones | Bean Validation, restricciones SQL y reglas de negocio |
| Bootstrap + JSP sin scriptlets | Vistas JSP con HTML, JSTL y EL; sin etiquetas Spring de formulario |
| JSF + Managed Bean | Registrar Sede con SedeBean y Facelet |
| Lógica transaccional | Crear estacionamiento y dos accesos juntos; registro de paso y control de aforo |
| Seguridad | Login, BCrypt, ADMIN/OPERADOR/CONSULTA, JWT en API y sesión/CSRF en web |

## Funciones adicionales

El aporte de operación incorpora porcentaje de ocupación, barra de aforo, filtros del historial y mensajes de estado. Detalles, pruebas y guion de demostración en [Operación y aforo](docs/Operacion-y-aforo.md).

- Creación automática de dos accesos por estacionamiento: ENTRADA y SALIDA.
- Pantalla de operación con registro manual o simulador de sensor, historial y luz virtual.
- Actualización cada 5 segundos; no es comunicación instantánea por WebSocket.
- Aforo calculado a partir de movimientos confirmados, sin contador duplicado.
- Bloqueo de la fila del estacionamiento durante cada operación: evita sobreaforo al registrar simultáneamente el último espacio.
- Eventos UUID idempotentes: reenviar el mismo evento con los mismos datos no vuelve a contarlo.
- Con placa, se rechazan entradas duplicadas y salidas sin entrada pendiente en esa zona. Detalles en [Control por placa](docs/Control-por-placa.md).
- Una zona inactiva rechaza entradas pero permite salidas pendientes.
- No reducir capacidad por debajo de ocupación ni eliminar una zona con historial. Se puede desactivar.
- Schema de dispositivos preparado para una siguiente etapa.

## Alcance y límites

Prototipo académico con autenticación y roles. La web utiliza sesión y CSRF; la API exige JWT. Las cuentas de demostración no sustituyen la gestión de usuarios de un sistema de producción. La placa es opcional: al proporcionarla se comprueba el último movimiento de esa placa en el estacionamiento; una salida sin placa solo puede consumir una entrada pendiente sin placa de esa zona. Aún no existe entidad Vehículo/Estadía ni reconocimiento de placas. No se vincula una salida sin placa con un vehículo identificado, ni se comprueba la presencia de una placa en otras zonas. Los sensores físicos y LEDs no están conectados. El origen SIMULADOR identifica eventos de prueba, no dispositivos autenticados.

El conteo comienza en cero. Antes de una prueba deben empezar con una zona vacía y coordinar que un cruce se registre una sola vez (manual o automático). Un sensor real detecta pasos sin saber si hay espacio; si ocurre un ingreso cuando está lleno o una salida sin ocupación, esta versión rechaza el registro. La integración real deberá conservar esos eventos como incidencias y permitir reconciliación auditada del aforo. También deberá gestionar eventos fuera de orden, desconexión y credenciales de dispositivo.

## API

| Método |                                         Ruta                                           |       Resultado       |
|--------|----------------------------------------------------------------------------------------|-----------------------|
| GET    | `/api/sedes` y `/api/sedes/{id}`                                                       | 200 / 404             |
| POST   | `/api/sedes`                                                                           | 201 / 400 / 409       |
| PUT    | `/api/sedes/{id}`                                                                      | 200 / 400 / 404 / 409 |
| DELETE | `/api/sedes/{id}`                                                                      | 204 / 404 / 409       |
| GET    | `/api/estacionamientos` y `/api/estacionamientos/{id}`                                 | 200 / 404             |
| POST   | `/api/estacionamientos`                                                                | 201 / 400 / 404 / 409 |
| PUT    | `/api/estacionamientos/{id}`                                                           | 200 / 400 / 404 / 409 |
| DELETE | `/api/estacionamientos/{id}`                                                           | 204 / 404 / 409       |
| GET    | `/api/estacionamientos/{id}/movimientos`                                               | 200 / 404             |
| POST   | `/api/estacionamientos/{id}/movimientos`                                               | 200 / 400 / 404 / 409 |
| GET    | `/api/estacionamientos/{id}/accesos` y `/api/estacionamientos/{id}/accesos/{accesoId}` | 200 / 404             |
| POST   | `/api/estacionamientos/{id}/accesos`                                                   | 201 / 400 / 404 / 409 |
| PUT    | `/api/estacionamientos/{id}/accesos/{accesoId}`                                        | 200 / 400 / 404 / 409 |
| DELETE | `/api/estacionamientos/{id}/accesos/{accesoId}`                                        | 204 / 404 / 409       |

Las rutas protegidas requieren `Authorization: Bearer <token>`.
Sin un token válido responden 401; si el usuario no tiene el permiso
necesario, responden 403. El login es público.

Ejemplo estacionamiento:

```json
{"sedeId":"AQP-PARRA","nombre":"Principal","capacidad":20,"activo":true}
```

Ejemplo ingreso; usar UUID nuevo por cruce y conservarlo al reintentar:

```json
{"eventoId":"581230cc-d8b9-493a-b600-a2a8804f9b9c","tipo":"ENTRADA","origen":"SIMULADOR","placa":null}
```

## Pruebas

En la verificación realizada se aprobaron 72 pruebas Java y 10 pruebas
JavaScript, sin fallos ni pruebas omitidas. También se comprobó el rollback
y la permanencia de los datos después de reiniciar la aplicación.

Las capturas están organizadas en [evidencias](evidencias/):
- [Seguridad](evidencias/seguridad/): login, petición autorizada y errores 401/403.
- [Persistencia](evidencias/persistencia/): datos antes y después del reinicio.
- [Pruebas](evidencias/pruebas/): resultados de Java, JavaScript y rollback.
- [Demostración](evidencias/demo/): CRUD, aforo, movimientos y roles.

Resultados del cierre técnico en [Verificación APF2](docs/Verificacion-APF2.md). Para los roles y Postman, ver [Seguridad APF2](docs/Seguridad-APF2.md).

```powershell
$env:JAVA_HOME = 'C:\ruta\al\jdk-21'
$env:TEST_DB_URL = 'jdbc:postgresql://localhost:5432/parkutp_test'
$env:TEST_DB_USER = 'parkutp'
$env:TEST_DB_PASSWORD = 'tu-clave'
.\scripts\Probar.ps1
```

Crear una base dedicada llamada `parkutp_test`. El script `Probar.ps1`
requiere configurar `TEST_DB_URL`, `TEST_DB_USER` y `TEST_DB_PASSWORD`.
Si faltan `TEST_DB_URL` o `TEST_DB_USER`, el script se detiene. La suite
trunca sus tablas antes de cada caso; no utilizar la base de trabajo.

La colección `postman/APF2-ParkUTP.postman_collection.json` prueba CRUD,
errores, aforo e idempotencia. Las evidencias del APF1 se conservan como
antecedentes; no son evidencia de pruebas del APF2.

## Fuentes técnicas

- [Spring Boot: JSP y empaquetado WAR](https://docs.spring.io/spring-boot/3.3/reference/web/servlet.html)
- [JoinFaces 5.2.5: compatibilidad e integración de JSF](https://docs.joinfaces.org/5.2.5/reference/)
- [PostgreSQL: bloqueo de filas](https://www.postgresql.org/docs/18/explicit-locking.html)
