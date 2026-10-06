# Aporte: operación, aforo e historial

La pantalla `/estacionamientos/{id}/operacion` amplía la operación existente con:

- Porcentaje de ocupación y barra verde por debajo del 80 %, amarilla desde el 80 % y roja al llegar al 100 %. Una zona inactiva muestra la barra gris.
- Filtros combinables por fecha, placa completa o parcial y tipo de movimiento. Buscar aplica los filtros; Limpiar vuelve al historial general.
- Actualización cada cinco segundos conservando los filtros aplicados.
- Mensajes que explican por qué no se permite una entrada o salida, y confirmación del tipo de movimiento y la placa registrada.
- Avisos ante pérdida de conexión, búsquedas vacías o consultas fallidas. El historial y el aforo se actualizan por separado.

## API del historial

`GET /api/estacionamientos/{id}/movimientos?fecha=2026-10-06&placa=ABC&tipo=ENTRADA`

Todos los parámetros son opcionales. La fecha usa `AAAA-MM-DD` y se interpreta en la zona `America/Lima` (Arequipa). La placa busca un fragmento literal sin distinguir mayúsculas; `%` y `_` no actúan como comodines. El tipo admite `ENTRADA` o `SALIDA`. Fechas o tipos inválidos y placas de más de doce caracteres devuelven HTTP 400.

Los filtros se aplican en PostgreSQL antes del límite de 50 movimientos, ordenados del más reciente al más antiguo. No alteran el aforo total. No hay paginación; si aparecen 50 resultados, precisar la búsqueda.

## Verificación

Desde la carpeta que contiene `pom.xml`:

```powershell
node --test scripts/operacion.test.cjs
mvn '-Dmaven.repo.local=.maven-cache' test
```

La prueba `HistorialApiTest` verifica solicitudes con filtros y errores de entrada sin necesitar PostgreSQL. La prueba de integración `historialFiltraAntesDelLimiteYRespetaDiaDeArequipa` verifica búsqueda de registros anteriores a los últimos 50, límites del día local y conservación del aforo; requiere la base dedicada `parkutp_test` indicada en el README.

Para comprobar la API manualmente, importar `postman/Operacion-ParkUTP.postman_collection.json` y configurar sus variables con un estacionamiento existente y datos registrados.

Resultado local del 6 de octubre de 2026: seis pruebas JavaScript y siete pruebas Java aprobadas. Las quince pruebas de integración se omitieron por falta de `TEST_DB_URL`. La compilación Java se ejecutó con `-Djava.version=17` por ser el JDK disponible; la configuración del proyecto sigue requiriendo Java 21. Queda pendiente verificar con Java 21, PostgreSQL y revisar la pantalla en el navegador.

## Guion para demostrar el aporte

1. Abrir la operación de un estacionamiento vacío y mostrar capacidad, ocupación, espacios libres y barra.
2. Registrar entradas con placas distintas y comprobar el porcentaje. Con capacidad cinco, cuatro entradas muestran amarillo; cinco muestran rojo y bloquean nuevos ingresos.
3. Registrar una salida y comprobar que vuelve a existir un espacio disponible.
4. Buscar por fecha, parte de una placa y tipo. Esperar cinco segundos y mostrar que los filtros se mantienen.
5. Buscar una placa inexistente y mostrar el estado vacío; pulsar Limpiar para volver al historial.
6. Guardar capturas reales de la pantalla y de los resultados de Postman como evidencia de esta entrega.
