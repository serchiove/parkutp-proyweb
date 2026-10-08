# ParkUTP — Avance 2

Evolución del proyecto APF1 para gestionar los estacionamientos de la UTP Arequipa. Conserva la API de sedes y añade persistencia PostgreSQL mediante JPA para sedes, estacionamientos y accesos, CRUD de estacionamientos, interfaz JSP/Bootstrap y un formulario JSF de sedes. Dos accesos separados: uno de entrada y otro de salida.

## Ejecutar en Windows

Requisitos: JDK 21, PostgreSQL instalado y acceso a Maven Central en la primera compilación. Bootstrap se incluye en el proyecto mediante WebJars: las pantallas no dependen de un CDN.

Desde esta carpeta, con PostgreSQL instalado y el puerto 55432 libre:

```powershell
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
.\scripts\Iniciar.ps1
```

Crear antes la base `parkutp` y un usuario con permisos sobre ella; la aplicación crea las tablas si no existen. No guardar contraseñas en Git. `schema.sql` sirve para una instalación inicial; cambios futuros del esquema necesitarán migraciones.

Abrir [Panel](http://localhost:8081/estacionamientos) y [Sedes JSF](http://localhost:8081/jsf/sedes.xhtml). Registrar primero una sede (ejemplo `AQP-PARRA` / `UTP Arequipa - Sede Parra`) y luego un estacionamiento con capacidad 20. No se incluyen capacidades reales ni datos institucionales inventados.

## Arquitectura y distribución

```text
controller/web    MVC: formularios JSP y navegación
controller/api    API REST y errores HTTP
view              SedeBean: formulario JSF administrado por Spring/JoinFaces
dto               Datos de entrada y validaciones
model             Sede, Estacionamiento, Movimiento
service           Reglas de negocio y límites transaccionales
dao               Adaptadores JPA; el INSERT de movimientos sigue con JDBC
entity            Sede, Estacionamiento, Acceso, Dispositivo y Movimiento mapeados con JPA
repository        CRUD Spring Data JPA y consultas JPQL de historial y aforo
src/main/webapp   JSP en WEB-INF y Facelet sedes.xhtml
resources         Esquema PostgreSQL, estilos y JS de operación
```

No hay SQL en los controllers, JSP ni el bean JSF. Los CRUD de sede, estacionamiento y acceso usan JPA/Hibernate. El historial y aforo se consultan por JPQL; el INSERT de movimientos todavía utiliza JdbcTemplate durante la migración de Sergio. El WAR permite ejecutar JSP con Tomcat embebido. JoinFaces integra Jakarta Faces 4 con Spring Boot 3.2.5. El término Managed Bean se implementa como bean administrado por Spring, visible en EL de JSF; no se usa la anotación histórica `javax.faces.bean.ManagedBean` de versiones antiguas.

## Adecuación al checklist APF2

Primera etapa implementada: entidad de movimientos, repositorio de lectura y consulta JPQL del historial con filtros antes del límite de 50 resultados. Hibernate no modifica el esquema existente. Detalles y verificación en [Migración del historial a JPQL](docs/JPQL-historial.md).

El CRUD JPA y la creación transaccional de estacionamiento y accesos están implementados. Detalles y pruebas del aporte de Melissa en [Persistencia JPA](docs/Persistencia-JPA-Melissa.md). Falta completar la escritura JPA de movimientos y agregar Spring Security con roles, JWT y sus pruebas de autorización.

## Funciones actuales

| Requisito | Implementación |
|---|---|
| CRUD de entidad principal | Estacionamiento: crear, listar, editar, eliminar desde JSP y REST |
| MVC, DAO, DTO | Paquetes separados, Service como capa adicional de negocio |
| Persistencia JPA | PostgreSQL + Hibernate + repositorios; escritura de movimientos aún JDBC |
| Validaciones | Bean Validation, restricciones SQL y reglas de negocio |
| Bootstrap + JSP sin scriptlets | Vistas JSP con HTML, JSTL y EL; sin etiquetas Spring de formulario |
| JSF + Managed Bean | Registrar Sede con SedeBean y Facelet |
| Lógica transaccional | Crear estacionamiento y dos accesos juntos; registro de paso y control de aforo |

## Funciones adicionales

El aporte de operación incorpora porcentaje de ocupación, barra de aforo, filtros del historial y mensajes de estado. Detalles, pruebas y guion de demostración en [Operación y aforo](docs/Operacion-y-aforo.md).

- Creación automática de dos accesos por estacionamiento: ENTRADA y SALIDA.
- Pantalla de operación con registro manual o simulador de sensor, historial y luz virtual.
- Actualización cada 5 segundos; no es comunicación instantánea por WebSocket.
- Aforo calculado a partir de movimientos confirmados, sin contador duplicado.
- Bloqueo de la fila del estacionamiento durante cada operación: evita sobreaforo al registrar simultáneamente el último espacio.
- Eventos UUID idempotentes: reenviar el mismo evento con los mismos datos no vuelve a contarlo.
- Una zona inactiva rechaza entradas pero permite salidas pendientes.
- No reducir capacidad por debajo de ocupación ni eliminar una zona con historial. Se puede desactivar.
- Schema de dispositivos preparado para una siguiente etapa.

## Alcance y límites

Prototipo académico, todavía sin autenticación ni roles; no publicar como sistema de producción. La placa es opcional y referencial: aún no existe entidad Vehículo/Estadía, reconocimiento de placas ni validación de doble ingreso por placa. Los sensores físicos y LEDs no están conectados. El origen SIMULADOR identifica eventos de prueba, no dispositivos autenticados.

El conteo comienza en cero. Antes de una prueba deben empezar con una zona vacía y coordinar que un cruce se registre una sola vez (manual o automático). Un sensor real detecta pasos sin saber si hay espacio; si ocurre un ingreso cuando está lleno o una salida sin ocupación, esta versión rechaza el registro. La integración real deberá conservar esos eventos como incidencias y permitir reconciliación auditada del aforo. También deberá gestionar eventos fuera de orden, desconexión y credenciales de dispositivo.

## API

| Método | Ruta | Resultado |
|---|---|---|
| GET | `/api/sedes` y `/api/sedes/{id}` | 200 / 404 |
| POST | `/api/sedes` | 201 / 400 / 409 |
| PUT | `/api/sedes/{id}` | 200 / 400 / 404 / 409 |
| DELETE | `/api/sedes/{id}` | 204 / 404 / 409 |
| GET | `/api/estacionamientos` y `/api/estacionamientos/{id}` | 200 / 404 |
| POST | `/api/estacionamientos` | 201 / 400 / 404 / 409 |
| PUT | `/api/estacionamientos/{id}` | 200 / 400 / 404 / 409 |
| DELETE | `/api/estacionamientos/{id}` | 204 / 404 / 409 |
| GET | `/api/estacionamientos/{id}/movimientos` | 200 / 404 |
| POST | `/api/estacionamientos/{id}/movimientos` | 200 / 400 / 404 / 409 |

Ejemplo estacionamiento:

```json
{"sedeId":"AQP-PARRA","nombre":"Principal","capacidad":20,"activo":true}
```

Ejemplo ingreso; usar UUID nuevo por cruce y conservarlo al reintentar:

```json
{"eventoId":"581230cc-d8b9-493a-b600-a2a8804f9b9c","tipo":"ENTRADA","origen":"SIMULADOR","placa":null}
```

## Pruebas

```powershell
$env:JAVA_HOME = 'C:\ruta\al\jdk-21'
$env:TEST_DB_URL = 'jdbc:postgresql://localhost:5432/parkutp_test'
$env:TEST_DB_USER = 'parkutp'
$env:TEST_DB_PASSWORD = 'tu-clave'
.\scripts\Probar.ps1
```

Crear una base dedicada llamada `parkutp_test`. La suite trunca sus tablas antes de cada caso. Sin TEST_DB_URL se ejecutan las pruebas independientes de la base y se omite la integración; eso no verifica JDBC ni transacciones. La colección `postman/APF2-ParkUTP.postman_collection.json` prueba CRUD, errores, aforo e idempotencia. Las evidencias del APF1 se conservan como antecedentes; no son evidencia de pruebas del APF2.

## Fuentes técnicas

- [Spring Boot: JSP y empaquetado WAR](https://docs.spring.io/spring-boot/3.3/reference/web/servlet.html)
- [JoinFaces 5.2.5: compatibilidad e integración de JSF](https://docs.joinfaces.org/5.2.5/reference/)
- [PostgreSQL: bloqueo de filas](https://www.postgresql.org/docs/18/explicit-locking.html)
