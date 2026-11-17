# Guion de cierre con Spring Boot y PostgreSQL

Estado actualizado el 17 de noviembre de 2026: recorrido real completado con 14 comprobaciones aprobadas y persistencia verificada después de reiniciar Spring Boot y la base local PostgreSQL 18.7. Se conservaron los cuatro movimientos con los mismos IDs y aforo cero. Capturas y resultados en `evidencias/api-real/`.

Las capturas de `evidencias/frontend-simulado/` corresponden a las 24 comprobaciones aisladas previas y siguen identificadas como simuladas. El guion siguiente permite repetir o ampliar la revisión manual; no implica que cada escenario adicional se haya automatizado. Los programas usados fueron `frontend/tests/ricardo.real.e2e.mjs` y `frontend/tests/ricardo.persistence.mjs`.

## Preparación

1. Levantar PostgreSQL y Spring Boot con la configuración original del equipo. La API debe responder en `http://localhost:8081/api`.
2. Contar con usuarios ADMIN, OPERADOR y CONSULTA, con sus credenciales reales de prueba.
3. Usar una sede y un estacionamiento dedicados a pruebas, creados por el módulo de Melissa o por el procedimiento que indique el coordinador. Evitar alterar datos de otros compañeros.
4. Iniciar la revisión con `node preview-ricardo.mjs` desde `frontend`, o usar `npm start` después de la integración de rutas.

## Recorrido y resultados esperados

| Paso | Acción | Resultado esperado |
| --- | --- | --- |
| 1 | Abrir Operación sin sesión | Login original; regreso a la pantalla después de autenticar |
| 2 | ADMIN abre Accesos y elige el estacionamiento | Lista real de accesos; normalmente ENTRADA y SALIDA creados automáticamente |
| 3 | Crear otro acceso del mismo tipo | Mensaje de conflicto de la API; no se duplica |
| 4 | Editar el nombre de un acceso | Nombre actualizado y tipo bloqueado |
| 5 | En un estacionamiento sin movimientos ni dispositivos, eliminar un acceso de prueba | Confirmación antes de DELETE y actualización del listado |
| 6 | Volver a crear el acceso eliminado | Acceso disponible para registrar movimientos |
| 7 | OPERADOR registra entrada de una placa de prueba | Éxito, ID de movimiento y aforo incrementado |
| 8 | Repetir entrada de la misma placa | Conflicto: ya está dentro; aforo sin incremento |
| 9 | Registrar salida de esa placa | Éxito y aforo reducido |
| 10 | Registrar otra salida de esa placa | Conflicto: no tiene una entrada pendiente |
| 11 | Registrar entrada y salida sin placa | `placa: null`; incremento y reducción del aforo |
| 12 | Intentar eliminar un acceso ya usado | Conflicto por movimientos asociados; historial conservado |
| 13 | Historial: combinar fecha local, parte de placa y ENTRADA | Solo movimientos que cumplen los tres filtros |
| 14 | Actualizar resultados | Se conservan filtros aplicados |
| 15 | Buscar placa inexistente y limpiar | Estado vacío; luego regreso al historial general |
| 16 | CONSULTA visita las tres pantallas | Puede consultar; no aparecen botones para modificar accesos o registrar movimientos |
| 17 | Desconectar temporalmente la API y actualizar | Error de conexión; al restablecer, recuperación mediante Actualizar |
| 18 | Reiniciar backend y volver a consultar | Movimientos conservados en PostgreSQL |

La creación/eliminación exitosa de accesos requiere un estacionamiento de prueba sin historial y sin dispositivos asociados. Los dos accesos iniciales pueden tener asociaciones; no se debe forzar su borrado ni modificar el backend para eludir sus reglas.

Para probar aforo lleno, usar un estacionamiento de capacidad pequeña y placas distintas. Para probar estado inactivo, coordinar el cambio con quien administra estacionamientos. Una salida válida debe seguir permitiéndose si existen vehículos dentro.

## Evidencias a guardar

Crear una carpeta aparte `evidencias/api-real/` y guardar:

- Accesos con nombre editado y mensaje de éxito.
- Confirmación de eliminación o conflicto por historial asociado.
- Ingreso confirmado con cambio de aforo.
- Salida confirmada con espacio liberado.
- Historial filtrado mostrando la placa y los tipos registrados.
- Vista de CONSULTA sin acciones de escritura.
- Resultado después de reiniciar el backend, para verificar persistencia.

Registrar fecha, entorno, usuario/rol de prueba y resultado por paso. No incluir contraseñas, JWT completos ni datos reales de personas en las capturas. No presentar las capturas simuladas como evidencia de integración o persistencia.
