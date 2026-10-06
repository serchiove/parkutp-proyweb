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
