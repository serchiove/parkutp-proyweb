# Aporte de Melissa: persistencia y CRUD JPA

Se migraron Sede, Estacionamiento y Acceso a entidades y repositorios Spring Data JPA. Dispositivo se mapea para conservar su relación con Acceso; no se incorpora hardware ni CRUD de dispositivos.

## Contratos y relaciones

- Sede conserva ID String de 64 caracteres. Estacionamiento, Acceso y Dispositivo conservan BIGINT generado por PostgreSQL.
- Estacionamiento tiene una sede; Acceso tiene un estacionamiento. No se configura cascade REMOVE en las entidades.
- MovimientoEntity conserva los campos estacionamientoId/accesoId y la consulta JPQL de Sergio. Se agregan relaciones ManyToOne de solo lectura sobre esas columnas para no romper su contrato ni duplicar escrituras.
- Los modelos de respuesta y DTO siguen separados de las entidades: no se serializan proxies JPA en JSON.
- Los DAO de sede y estacionamiento ahora son adaptadores JPA. Mantener sus nombres evita cambios innecesarios en los servicios, JSP y JSF. No contienen JdbcTemplate ni SQL.
- El historial y el aforo se consultan mediante JPQL. El aforo continúa derivándose de movimientos; no se agrega un contador independiente.

## Transacciones y reglas

EstacionamientoService conserva la transacción que crea la zona y los dos accesos. Se utiliza PESSIMISTIC_WRITE en la fila de estacionamiento para cambios de capacidad, accesos y entradas/salidas. Se preservan UUID único, reglas de lleno/vacío/inactivo, capacidad mínima y rechazo de eliminación con historial.

El CRUD de accesos usa `/api/estacionamientos/{estacionamientoId}/accesos`: GET lista, GET /{id}, POST crea, PUT /{id} cambia nombre y DELETE /{id} elimina si no tiene movimientos/dispositivos. Solo admite ENTRADA/SALIDA y un acceso por tipo. El tipo no se cambia después de crearlo. Si falta un acceso, registrar un movimiento de ese tipo devuelve 409 hasta reponerlo. Al crear un estacionamiento ya se generan ambos accesos; por eso intentar crear otro del mismo tipo devuelve 409.

`schema.sql` sigue creando las tablas iniciales y los índices únicos insensibles a mayúsculas. Hibernate usa `ddl-auto=validate`: verifica el mapeo sin recrear ni modificar tablas. Cambios futuros del esquema requieren una migración explícita. Los datos demo no se borran.

## Verificación del 8 de octubre de 2026

JDK 21.0.12 y PostgreSQL 18, base dedicada `parkutp_test` en una instancia temporal del puerto 55439. No se utilizó la base demo `parkutp`.

- 35 pruebas Java aprobadas: 20 de integración PostgreSQL, 4 de repositorio H2, 5 de API del historial, 4 de formulario MVC y 2 de DTO. Sin fallos ni omisiones.
- 6 pruebas JavaScript aprobadas.
- Rollback real: un trigger de prueba falla al insertar SALIDA después de insertar zona y ENTRADA; al terminar no queda ninguna zona ni acceso parcial. Se elimina el trigger al finalizar.
- Protección de historial y dispositivos; duplicados; CRUD de accesos; respuestas 400/404/409; concurrencia por el último espacio.
- Empaquetado WAR completado.
- WAR iniciado con PostgreSQL: API, JSP de edición y listado de sedes JSF respondieron correctamente. Una sede y zona de prueba conservaron sus datos después de detener y reiniciar la aplicación. Se eliminaron únicamente esos registros de prueba.

Comando general, configurando antes TEST_DB_URL/TEST_DB_USER/TEST_DB_PASSWORD para una base dedicada llamada parkutp_test:

```powershell
$env:JAVA_HOME = 'C:\ruta\al\jdk-21'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\scripts\Probar.ps1
node --test scripts/operacion.test.cjs
```

En el entorno de revisión fue necesario cargar previamente el agente de Byte Buddy para las pruebas de Mockito; la conexión dinámica del agente está restringida. No se cambió esa configuración en producción:

```powershell
$agentePruebas = Join-Path (Get-Location) '.maven-cache\net\bytebuddy\byte-buddy-agent\1.14.13\byte-buddy-agent-1.14.13.jar'
.\mvnw.cmd -o '-Dmaven.repo.local=.maven-cache' "-DargLine=-javaagent:$agentePruebas" package
```

## Coordinación pendiente

Sergio conserva el registro de movimientos: el INSERT de MovimientoDao todavía usa JDBC. Esta entrega migra el CRUD asignado a Melissa y la creación transaccional de zona/accesos; no declara terminada la migración completa de movimientos. Ricardo debe incorporar usuarios, roles, Spring Security y JWT, incluyendo protección de los formularios equivalentes a la API. Esta entrega no completa por sí sola el checklist APF2.
