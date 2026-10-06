# Demo APF2 — 6 a 7 minutos

1. Problema (40 s): ambos vigilantes coordinan por radio; el sistema comparte disponibilidad entre entrada y salida.
2. Arquitectura (50 s): abrir controller, DTO, Service y DAO. Mostrar que solo DAO tiene SQL; JdbcTemplate usa JDBC.
3. JSF (50 s): registrar sede; mostrar validación de nombre/código y duplicado. Explicar SedeBean y el formulario Facelet.
4. CRUD (90 s): crear estacionamiento de capacidad 20, listar, editar y eliminar una zona temporal sin movimientos. Mostrar validación de capacidad 0.
5. Extra de aforo (70 s): abrir operación en dos ventanas; simular entrada -> 19 disponibles; salida -> 20. Mostrar historial y explicar que la luz todavía es virtual.
6. Transacción (40 s): mostrar @Transactional y SELECT FOR UPDATE. Explicar prueba de dos ingresos para el último espacio y eventos UUID idempotentes.
7. Cierre (30 s): reiniciar y mostrar persistencia. Indicar próximas etapas: usuarios, identificación por placa, integración de sensores reales.

No presentar simulación como hardware conectado. No presentar la suite antigua como prueba de la nueva versión. Llevar capturas actuales de CRUD, formulario JSF y resumen de pruebas.
