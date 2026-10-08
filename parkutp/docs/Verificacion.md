# Verificación ejecutada

- PostgreSQL real iniciado para verificación
- Maven package: BUILD SUCCESS
- WAR ejecutable inició con PostgreSQL
- JSF: formulario real enviado y sede persistida
- JSP: creación desde formulario MVC y listado renderizado
- HTTP: ingreso, reenvío idempotente y salida correctos
- HTTP: CRUD JSP completo, validación y Bootstrap local verificados
- Persistencia comprobada tras reiniciar el WAR

- Suite: 16 pruebas, 0 fallos, 0 errores, 0 omitidas (14 de integración y 2 de validación).
- No se completó inspección visual en navegador: el navegador del escritorio no pudo acceder al servidor aislado de las herramientas. Las páginas JSP y JSF sí se verificaron mediante solicitudes HTTP y envío de sus formularios reales.

## Ajuste JSP del 7 de octubre de 2026

Se reemplazaron las etiquetas Spring de formulario por HTML, JSTL y EL. El controlador entrega los valores enviados y los errores de validación; el campo oculto `_activo` permite desmarcar correctamente la casilla al editar. Los valores se escapan con `c:out`.

Verificación focalizada: `mvnw.cmd -o -Dmaven.repo.local=.maven-cache -Dtest=FormularioEstacionamientoTest test`, con JDK 21. Cuatro pruebas aprobadas, ninguna omitida: capacidad no numérica y conservación de datos, casilla marcada/desmarcada y errores de negocio. Estas pruebas verifican el controlador; queda pendiente comprobar la vista renderizada en el navegador y ejecutar la integración completa con PostgreSQL.

## Migración JPA del 8 de octubre de 2026

Sede, Estacionamiento y Acceso usan entidades y repositorios JPA. Resultado actual: 35 pruebas Java aprobadas, sin fallos, errores ni omisiones, y 6 pruebas JavaScript aprobadas. Se ejecutaron 20 pruebas de integración con PostgreSQL 18 en una base dedicada, incluyendo rollback ante fallo al crear el segundo acceso y conservación del historial/dispositivos.

El WAR compiló e inició con JDK 21. Se comprobó mediante HTTP la API, el JSP de edición y el listado JSF; los datos de prueba permanecieron después de reiniciar la aplicación. No se realizó inspección visual adicional en navegador en esta verificación.

Detalle y límites de la entrega en Persistencia-JPA-Melissa.md; salida de Maven en Resultados-pruebas-jpa.txt. La inserción de movimientos aún usa JDBC y la seguridad/JWT sigue pendiente del equipo.
