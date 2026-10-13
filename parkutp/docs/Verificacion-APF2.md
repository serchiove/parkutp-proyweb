# Verificación del cierre técnico APF2

Fecha: 12 de octubre de 2026. Base de los cambios: `main` en `0b49da6`, que integra el PR #20. Rama de trabajo: `sergio-seguridad-completa`.

| Comprobación | Resultado |
|---|---|
| Maven `package`, JDK 21 y PostgreSQL | BUILD SUCCESS |
| Pruebas Java | 72 ejecutadas, 0 fallos, 0 errores, 0 omitidas |
| JavaScript, `node --test scripts/operacion.test.cjs` | 10 aprobadas |
| Colección APF2 por HTTP | 16 solicitudes, 19 aserciones aprobadas |
| Colección Seguridad por HTTP | 10 solicitudes, 10 aserciones aprobadas |
| Colección Operación por HTTP | 6 solicitudes, 9 aserciones aprobadas |

Las solicitudes y los scripts de aserciones de las colecciones se ejecutaron con un ejecutor temporal de Node, contra el WAR real. No se utilizó la interfaz de Postman ni Newman. Para la entrega académica, importar las colecciones en Postman y capturar su ejecución.

Se comprobó adicionalmente por HTTP el HTML generado por Tomcat y Faces: login con CSRF, sesión de los tres roles, JSP con navegación y acciones correspondientes, alta real de sede con formulario JSF y ViewState, alta de estacionamiento desde JSP, movimiento autorizado, denegación 403 de escrituras de OPERADOR/CONSULTA, API sin Bearer rechazada con 401 y cierre de sesión con CSRF. Esta comprobación no sustituye una revisión visual de las pantallas en el navegador.

Las pruebas se ejecutaron en PostgreSQL temporal sobre `127.0.0.1:55433`, base `parkutp_test`, y el WAR en `8082`. No se utilizaron ni borraron los datos de la base de trabajo en `5432`. Los puertos temporales no cambian la configuración habitual `8081`/`5432`.

Las pruebas Java conservan la cobertura del negocio: aforo, idempotencia, placas, entradas sin placa, concurrencia y rollback ante fallo del registro. Se añade autorización de roles, JWT inválido/vencido, login incorrecto, cuentas desactivadas, cambios de rol, CSRF y sesión web.

Para repetir la suite y preparar las capturas del equipo, seguir [Seguridad APF2](Seguridad-APF2.md). El informe final debe incluir las evidencias de su ejecución y la explicación de JPA/JPQL y las transacciones. Las propuestas futuras de reservas, espacios accesibles, dispositivos físicos e integración con la app UTP continúan fuera de esta implementación.
