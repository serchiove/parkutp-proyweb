# Control de entradas y salidas con placa

Una placa proporcionada debe alternar ENTRADA y SALIDA dentro de cada estacionamiento. Una entrada repetida mientras su último movimiento es ENTRADA devuelve 409. Una salida sin movimiento anterior, o cuyo último movimiento es SALIDA, también devuelve 409. La operación rechazada no guarda movimientos ni cambia el aforo.

Se consulta el último movimiento de la placa exacta, sin distinguir mayúsculas, mediante Spring Data JPA. El orden de registro se determina por ID; no depende de los filtros del historial, del límite de 50 ni de ajustes de fecha. Las validaciones se ejecutan bajo el bloqueo del estacionamiento en la misma transacción del registro. El UUID se comprueba primero para que los reintentos de un evento confirmado sigan devolviendo su registro original incluso después de otros cruces.

La placa sigue siendo opcional tanto para MANUAL como para SIMULADOR. Una salida sin placa solo se permite si la suma de entradas sin placa menos salidas sin placa es positiva en ese estacionamiento. Este saldo se calcula con JPQL sobre todo el historial, bajo el mismo bloqueo transaccional; no depende de los filtros ni del límite de 50. Una salida anónima no consume el cupo de una placa identificada. El control se aplica en ambos orígenes. En una zona con historial válido, el aforo total es igual a placas dentro más entradas anónimas pendientes.

No se comprueba presencia entre distintas zonas. Los cruces anónimos no identifican vehículos individuales. La siguiente etapa de seguimiento completo requerirá vincular entradas y salidas en una estadía.

No se corrige ni elimina el historial anterior. Los registros incorrectos de demostración ya existentes requieren conciliación; para comprobar la nueva regla, crear una zona nueva y vacía.

## Prueba manual

1. Crear una zona con capacidad 2.
2. Registrar ENTRADA de ABC y ENTRADA de ABC2: quedan 2 vehículos.
3. Registrar SALIDA de ABC2: queda 1 vehículo.
4. Intentar otra SALIDA de ABC2: aparece conflicto y sigue 1 vehículo dentro.
5. Intentar otra ENTRADA de ABC: aparece conflicto.
6. Registrar SALIDA de ABC: queda vacía.
7. Registrar nueva ENTRADA de ABC: se permite.
8. Registrar una ENTRADA sin placa y una SALIDA sin placa: ABC continúa dentro.
9. Intentar otra SALIDA sin placa: aparece conflicto y ABC sigue dentro.

## Verificación

Se agregaron seis pruebas de integración PostgreSQL: reproducción del doble egreso con respuesta HTTP 409 y aforo correcto, entrada duplicada sin distinguir mayúsculas, salida desconocida con cruces anónimos, reingreso e idempotencia, aislamiento por estacionamiento y dos entradas simultáneas de la misma placa. La suite completa pasó 43 pruebas Java con JDK 21 y PostgreSQL temporal, sin fallos ni omisiones; el WAR también se empaquetó correctamente. La base temporal está separada de los datos de la web del usuario.

La corrección de cruces anónimos incorpora otras cuatro pruebas: doble salida anónima con otro vehículo identificado dentro, reintento UUID, dos salidas simultáneas con una sola entrada anónima y aislamiento entre zonas. El caso mezclado comprueba HTTP 409 y que permanezca el vehículo identificado.

Resultado de esta última corrección: 47 pruebas Java aprobadas, sin fallos ni omisiones, con Java 21 y PostgreSQL temporal; 32 son pruebas de integración.
