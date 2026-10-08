# Segunda etapa de Sergio: registro de movimientos con JPA

La rama incorpora la versión de main con el aporte de Melissa (3455fa5). Se conserva su mapeo de Sede, Estacionamiento, Acceso y Dispositivo y se completa la persistencia de Movimiento. MovimientoDao ya no contiene JdbcTemplate ni INSERT SQL.

## Comportamiento

MovimientoService conserva la transacción y el bloqueo PESSIMISTIC_WRITE sobre el estacionamiento. Valida aforo, estado y UUID antes de guardar. MovimientoRepository expone saveAndFlush con transacción de escritura; las consultas mantienen su configuración de solo lectura.

La fecha registrado_en continúa generándose mediante CURRENT_TIMESTAMP de PostgreSQL. La entidad omite esa columna al insertar, y Hibernate recupera el valor generado para devolver una respuesta completa, incluida la fecha. El UUID se conserva al reintentar: un evento repetido con los mismos datos devuelve el registro original.

Se mantienen los identificadores escalares y las asociaciones de solo lectura que agregó Melissa. No se cambian el esquema, los DTO, las rutas, el JSON ni las pantallas. Los fallos de integridad de JPA se traducen a conflicto HTTP 409. El historial se conserva como registro de eventos; no se agregan edición ni borrado de movimientos.

## Verificación del 8 de octubre de 2026

- 37 pruebas Java aprobadas, sin fallos ni omisiones: 22 de integración PostgreSQL y 15 de repositorio, formularios, API y validaciones.
- 6 pruebas JavaScript del panel aprobadas mediante `node --test scripts/operacion.test.cjs`.
- Dos casos nuevos verifican que la respuesta incluya la fecha exacta generada por PostgreSQL y que un fallo controlado al insertar no altere el aforo ni impida reintentar después.
- La suite existente comprueba entradas/salidas, último espacio concurrente, eventos repetidos, filtros e integridad del historial y el rollback de creación de zona y accesos.
- Se utilizó una instancia temporal de PostgreSQL 18 en el puerto 55433, base parkutp_test, separada de los datos demo.

La primera ejecución utilizó Java 17. Después se descargó un JDK portátil oficial Eclipse Adoptium 21.0.12.1, se verificó su suma SHA256 y se ejecutó `mvn -B -Dmaven.repo.local=.maven-cache clean package` con JAVA_HOME y PATH temporales. Las 37 pruebas también pasaron con Java 21, sin fallos ni omisiones, y se generó correctamente `target/parkutp-apf2.war` (BUILD SUCCESS). No se modificó la instalación global de Java ni la versión del proyecto.

## Coordinación

La rama Ricky contiene una implementación alternativa de persistencia basada en una versión anterior. Esta entrega completa Movimiento sobre el main actualizado; antes de fusionar Ricky se deben comparar y seleccionar sus cambios para evitar sobrescribir el historial JPQL y las entidades actuales. Ricardo tiene pendiente la seguridad con usuarios, roles y JWT, incluyendo formularios web y rutas REST. No se implementan reservas ni sensores físicos en esta etapa.
