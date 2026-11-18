# Cierre de integración APF3

Revisión local del 17 de noviembre de 2026, sobre main dd81e99 y las correcciones de esta entrega.

## Alcance implementado

- Angular: login, rutas protegidas, interceptor JWT y navegación según ADMIN, OPERADOR y CONSULTA.
- Sedes y estacionamientos: altas, edición, consulta, confirmación de eliminación y validaciones.
- Accesos: CRUD con mensajes de conflicto y protección del historial.
- Operación: ingresos/salidas, aforo y reintentos mediante UUID.
- Historial: filtros por fecha de Perú, placa y tipo.
- Formularios de administración: aviso antes de abandonar cambios sin guardar.

## Correcciones de cierre

Las pruebas de Ricardo todavía buscaban dist/ricardo/browser. Ahora todas usan dist/frontend/browser. npm test compila y ejecuta los tres recorridos simulados. Playwright forma parte de las dependencias de desarrollo. El modal utiliza la misma tipografía base de la aplicación. La suite del modal cierra conexiones HTTP al terminar.

## Verificación

- Compilación Angular de producción correcta.
- API simulada: 13 comprobaciones de administración, 4 del aviso de cambios y 25 de integración/operación.
- API real: 14 comprobaciones de acceso, operación, historial y permisos contra el backend local y PostgreSQL.
- API real de administración: creación y edición de sede; creación/edición de estacionamiento y sus dos accesos; conflicto 409 al eliminar una sede asociada; eliminación de los registros exclusivos de prueba. Sin errores JavaScript.
- Pruebas JavaScript de la interfaz anterior: 10 aprobadas.
- Auditoría de dependencias de producción: cero vulnerabilidades reportadas por npm audit --omit=dev.
- Las capturas de docs/melissa y docs/ricardo se actualizan durante los recorridos.

El recorrido de Ricardo deja una sede RIC- y un estacionamiento de prueba con cuatro movimientos y ocupación cero como evidencia. No se borraron registros preexistentes. Las pruebas de Melissa eliminan sus propios registros.

## Límites de esta verificación

No se reejecutó la suite Java ni la prueba de persistencia tras reiniciar el servidor. Los resultados actuales de API real no deben confundirse con el reinicio documentado en la entrega anterior.

Angular muestra advertencias de presupuesto CSS (entre 5 y 8 kB por componente); la compilación termina correctamente. npm audit reporta 11 alertas altas en la cadena de herramientas de desarrollo, originadas por picomatch (GHSA-fc5q-39xc-vjpf). El informe no ofrece una actualización compatible para toda la cadena. No se ejecutó audit fix --force, que propone degradar Angular CLI. Revisar una actualización compatible antes de publicar un entorno de desarrollo en Internet.

La entrega queda verificada para el alcance funcional APF3. La publicación en hosting, sensores físicos, reservas y administración de cuentas no forman parte de este cierre.
