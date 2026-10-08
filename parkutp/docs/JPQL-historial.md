# Primera etapa de Sergio: historial mediante JPA y JPQL

El historial de movimientos y la búsqueda de eventos por UUID ahora utilizan Spring Data JPA. La pantalla de operación y la ruta `GET /api/estacionamientos/{id}/movimientos` conservan sus filtros y su formato de respuesta.

## Cambios

- `entity/MovimientoEntity.java` representa la tabla existente `movimiento`.
- `repository/MovimientoRepository.java` consulta el historial mediante JPQL y busca eventos por UUID.
- `dao/MovimientoDao.java` adapta los resultados al modelo `Movimiento` que consumen la API y las vistas.
- `pom.xml` incorpora Spring Data JPA y H2 únicamente para las pruebas.
- `application.properties` establece `ddl-auto=none` y desactiva Open EntityManager in View.

Los filtros por estacionamiento, fecha, placa y tipo se aplican antes de seleccionar los 50 movimientos más recientes. La fecha corresponde al día en America/Lima; el límite superior es exclusivo. La placa se busca parcialmente sin distinguir mayúsculas, y `%` y `_` se tratan como caracteres literales. Para evitar problemas de tipado de parámetros nulos en PostgreSQL, el repositorio envía valores no nulos y activa los límites de fecha con indicadores booleanos. La fecha de referencia no limita las consultas sin filtro.

## Alcance de esta etapa y coordinación

El registro de entradas/salidas y los demás CRUD siguen usando JDBC. La entidad mantiene los identificadores de estacionamiento y acceso como campos: las asociaciones JPA se deben coordinar con las entidades que prepare Melissa. Ricardo continúa con autenticación, roles y JWT. No se incorporaron reservas ni funcionalidades futuras.

Se conserva el bloqueo del estacionamiento, las reglas de aforo y la protección contra eventos repetidos. Las lecturas JPA participan en las transacciones del servicio cuando ya existe una transacción; la integración se comprobó usando PostgreSQL real.

## Verificación

Se ejecutaron 30 pruebas Java sin fallos ni omisiones: 4 nuevas del repositorio, 15 de integración con PostgreSQL y 11 de formularios, filtros y validaciones. También pasaron las 6 pruebas JavaScript del panel. Las pruebas de PostgreSQL utilizaron una instancia temporal en el puerto 55433 y una base dedicada `parkutp_test`, separada de los datos de demostración.

El entorno de verificación disponible tenía JDK 17, por lo que Maven se ejecutó con `-Djava.version=17`. El proyecto conserva Java 21 como requisito; queda pendiente repetir la compilación con ese JDK.

Para repetir las pruebas con JDK 21, desde la carpeta que contiene `pom.xml`, configurar una base dedicada:

```powershell
$env:JAVA_HOME = 'C:\ruta\al\jdk-21'
$env:TEST_DB_URL = 'jdbc:postgresql://localhost:5432/parkutp_test'
$env:TEST_DB_USER = 'tu_usuario'
$env:TEST_DB_PASSWORD = 'tu_clave'
.\scripts\Probar.ps1
```

La suite de integración vacía las tablas de `parkutp_test` antes de cada caso. Sin `TEST_DB_URL`, Maven omite esas pruebas; las pruebas del repositorio usan H2 y no sustituyen la verificación con PostgreSQL.
