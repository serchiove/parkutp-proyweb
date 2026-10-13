# Seguridad y verificación del APF2

Se completa la base de autenticación aportada por Ricardo con autorización real en la API y en las pantallas. Se mantienen la persistencia JPA, las consultas JPQL, las transacciones, el control de placas y el aforo.

## Permisos

| Acción | ADMIN | OPERADOR | CONSULTA |
|---|---|---|---|
| Ver sedes, estacionamientos, aforo e historial | Sí | Sí | Sí |
| Registrar entradas y salidas | Sí | Sí | No |
| Crear, editar o eliminar sedes y estacionamientos | Sí | No | No |

Las restricciones se aplican en el servidor; ocultar un botón no sustituye la autorización. El formulario JSF de sedes también queda protegido por rol y CSRF. El método de guardado del bean exige ADMIN.

## Iniciar con PostgreSQL existente

Crear la base `parkutp` antes de iniciar. Desde la carpeta que contiene `mvnw.cmd`, usar PowerShell:

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/parkutp'
$env:DB_USER = 'postgres'
$claveDb = Read-Host 'Contraseña de PostgreSQL' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $claveDb).Password
$claveDemo = Read-Host 'Contraseña para las tres cuentas de demostración' -AsSecureString
$env:DEMO_PASSWORD = [System.Net.NetworkCredential]::new('', $claveDemo).Password
.\scripts\Iniciar.ps1
```

Se crean `admin`, `operador` y `consulta`, con la contraseña introducida y sus roles respectivos, solo si todavía no existen. Las contraseñas se guardan con BCrypt. Cambiar DEMO_PASSWORD no cambia cuentas existentes. La contraseña de PostgreSQL y la de estas cuentas cumplen funciones diferentes.

Abrir `http://localhost:8081/login`. Si ya estaba ejecutándose una versión anterior, detenerla con Ctrl+C antes de iniciar esta versión. Las variables pertenecen a la terminal actual: otra terminal necesita su propia configuración.

Para la instancia local independiente se puede usar `Iniciar.ps1 -DemoLocal`; configurar igualmente DEMO_PASSWORD si se necesitan cuentas nuevas.

## Sesión web y JWT

- La web usa sesión de servidor, cookie HttpOnly y formularios con token CSRF, incluido cerrar sesión.
- La API exige `Authorization: Bearer <token>`. Una cookie de sesión por sí sola no autoriza la API.
- `POST /api/auth/login` entrega el JWT; `GET /api/auth/yo` confirma la identidad.
- La pantalla de operación obtiene su JWT en `GET /sesion/token`, protegido por sesión y con `Cache-Control: no-store`. El token permanece en memoria JavaScript. No se guarda en localStorage.
- Al vencer el JWT, la pantalla solicita otro una sola vez y reintenta conservando el UUID del movimiento. Al vencer la sesión, pide iniciar sesión.
- Usuarios desactivados pierden acceso. Un cambio de rol se aplica a la sesión y hace que los JWT con el rol anterior sean rechazados.
- Sin token o con token inválido: 401. Usuario autenticado sin permiso: 403. Login incorrecto: mensaje genérico.

JWT_SECRET debe configurarse para conservar la clave entre reinicios. Puede generarse una clave aleatoria para el entorno local:

```powershell
$bytesJwt = New-Object byte[] 64
$rngJwt = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rngJwt.GetBytes($bytesJwt)
$rngJwt.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($bytesJwt)
```

Conservar esa clave de forma privada si se desea reutilizarla; no subirla al repositorio. Sin ella se genera una clave temporal al arrancar, invalidando los tokens anteriores. Los JWT duran por defecto 60 minutos. Cerrar la sesión web no revoca un JWT ya emitido; ese token vence por tiempo o deja de servir si se desactiva la cuenta o cambia su rol. Usar HTTPS al desplegar fuera de localhost.

## Repetir las pruebas

Crear una base exclusiva `parkutp_test`. La suite borra sus tablas: no usar la base `parkutp`.

```powershell
$env:TEST_DB_URL = 'jdbc:postgresql://localhost:5432/parkutp_test'
$env:TEST_DB_USER = $env:DB_USER
$env:TEST_DB_PASSWORD = $env:DB_PASSWORD
.\scripts\Probar.ps1
node --test .\scripts\operacion.test.cjs
```

La verificación automatizada cubre el negocio, la persistencia, los roles, los tokens inválidos/vencidos, usuarios desactivados, cambios de rol, CSRF, login, logout y el puente sesión/JWT. El empaquetado se verifica con JDK 21 y PostgreSQL en una instancia temporal separada.

También se prueba por HTTP el WAR real: renderizado del login y las JSP, creación de sede mediante JSF con ViewState y CSRF, creación de estacionamiento, operación autorizada, lectura por CONSULTA y denegación de escrituras por rol. Esta comprobación complementa MockMvc, que no ejecuta el renderizador JSP/JSF.

## Postman y demostración

Importar `postman/Seguridad-ParkUTP.postman_collection.json` y configurar `baseUrl` y `demoPassword` localmente. Ejecutar los tres login y después las solicitudes de permisos. Se comprueban respuestas 200, 401 y 403 sin modificar datos. Para CRUD, aforo, errores de negocio e idempotencia usar `APF2-ParkUTP.postman_collection.json`; ahora incluye login y Bearer. La colección de operación también incluye autenticación. No exportar contraseñas ni tokens guardados durante las pruebas.

Guion manual:

1. Entrar como `admin`: crear sede en JSF y estacionamiento; editar y comprobar validaciones.
2. Cerrar sesión y entrar como `operador`: ver aforo e historial, registrar entrada y salida, verificar duplicados y entradas pendientes sin placa. No debe poder entrar a crear/editar/eliminar.
3. Entrar como `consulta`: ver aforo e historial sin botones de registro ni administración. Una petición escrita manualmente debe seguir devolviendo 403.
4. En Postman, consultar sin token (401), con token autorizado (200) y con rol insuficiente (403).
5. Reunir capturas, resultados de pruebas y explicación de la transacción con rollback para el informe del equipo. Las colecciones están preparadas; las evidencias de una ejecución de Postman deben obtenerse al ejecutarlas.

No se incluyen reservas, reconocimiento de placas, plazas accesibles ni integración con la app UTP; continúan como propuestas futuras. Los dispositivos físicos siguen pendientes de una etapa posterior. No se incluye administración de cuentas desde una pantalla.

La contraseña de base de datos que estuvo escrita en versiones anteriores debe cambiarse en PostgreSQL. Quitarla del archivo actual no la elimina del historial de Git.
