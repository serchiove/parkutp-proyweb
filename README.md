<div align="center">

# ParkUTP

### Gestión de estacionamientos universitarios

**Desarrollo Web Integrado · Avance 3**<br>
**Angular · Spring Boot · PostgreSQL**

Sergio · Melissa · Ricardo

</div>

ParkUTP centraliza la administración de sedes y estacionamientos, registra ingresos y salidas y permite consultar la disponibilidad y el historial de movimientos. La interfaz Angular utiliza la API REST existente, con autenticación JWT y permisos por rol.

Los registros utilizados en la demostración son datos de prueba; no representan capacidades ni movimientos institucionales reales. La disponibilidad se calcula con los movimientos registrados, sin sensores físicos conectados.

## Navegación rápida

- [Ejecutar en Windows](#ejecutar-en-windows)
- [Pantallas y permisos](#pantallas-y-permisos)
- [Arquitectura y estructura](#arquitectura-y-estructura)
- [Configuración](#configuración)
- [Verificación y pruebas](#verificación-y-pruebas)
- [Demostración](#demostración)
- [Solución de problemas](#solución-de-problemas)
- [Documentación y equipo](#documentación-y-equipo)

## Funcionalidades actuales

| Módulo | Qué permite hacer |
|---|---|
| Sedes | Listar, crear, editar el nombre y eliminar cuando no existen dependencias. |
| Estacionamientos | Administrar sede, nombre, capacidad y estado; generar los accesos iniciales de entrada y salida. |
| Accesos | Consultar y administrar los puntos de entrada y salida de cada estacionamiento. |
| Operación | Registrar movimientos, consultar ocupados y disponibles y actualizar el aforo. |
| Historial | Buscar movimientos por fecha local de Perú, placa completa o parcial y tipo. |
| Seguridad | Iniciar y cerrar sesión, enviar JWT mediante interceptor y aplicar permisos de ADMIN, OPERADOR y CONSULTA. |
| Formularios | Validar datos, mostrar errores y confirmaciones, evitar envíos repetidos y advertir sobre cambios sin guardar en sedes y estacionamientos. |

En Angular el aforo se actualiza al seleccionar un estacionamiento, registrar un movimiento o solicitar una actualización. **No hay actualización automática cada cinco segundos en esta interfaz.** Las vistas JSP/JSF del avance anterior siguen en el backend como implementación previa.

## Ejecutar en Windows

### Requisitos

| Herramienta | Requisito del proyecto |
|---|---|
| Java | JDK 21 completo, con `java` y `javac`. |
| PostgreSQL | Ejecutables instalados; la demo local se verificó con PostgreSQL 18. |
| Node.js y npm | Node 24.15 o posterior dentro de la rama 24; también se admite Node 22.22.3 o posterior dentro de la rama 22, según el CLI instalado. |
| Git | Clonar y actualizar el repositorio. |
| Maven | Se utiliza el Wrapper incluido; no requiere instalación independiente. |

Comprueba el entorno en una terminal PowerShell:

```powershell
java -version
javac -version
node --version
npm --version
```

La primera instalación descarga dependencias de Maven y npm. No necesitas instalar Angular CLI globalmente.

### 1. Abrir la raíz del repositorio

Si aún no tienes una copia:

```powershell
git clone https://github.com/serchiove/parkutp-proyweb.git
cd .\parkutp-proyweb
```

Abre esta carpeta en VS Code. La **raíz Git** contiene `README.md`, `frontend`, `parkutp` y `docs`. El proyecto Java está dentro de `parkutp`; Angular está dentro de `frontend`.

### 2. Terminal del backend y la base local

Desde la raíz Git:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\docs\ricardo\Iniciar-PruebaLocal.ps1
```

Este iniciador prepara un `.env` local si no existe, carga `JWT_SECRET`, `DEMO_PASSWORD` y `CORS_ORIGENES`, e inicia el script original en modo `DemoLocal`. Compila el WAR y arranca Spring Boot. Mantén la terminal abierta y espera el mensaje de inicio de la aplicación.

| Servicio | Dirección predeterminada |
|---|---|
| API REST | `http://localhost:8081/api` |
| PostgreSQL de demostración | `127.0.0.1:55432` |
| Base y usuario locales | `parkutp` / `parkutp_local` |
| Datos persistentes | `parkutp/.local-data/` |

La demo usa una instancia independiente y autenticación PostgreSQL de confianza local. No modifica el servicio habitual de PostgreSQL en 5432. Este modo es para demostración local.

El script intenta detectar Adoptium JDK 21 y PostgreSQL en sus carpetas de instalación habituales. Si no encuentra Java, define `JAVA_HOME` con la ruta real del JDK. Para otra instalación de PostgreSQL, consulta el [inicio manual y la opción `-PgBin`](docs/Guia-APF2.md#4-demo-local-independiente).

**Cuentas:** `admin`, `operador` y `consulta`. La contraseña inicial está en `DEMO_PASSWORD` del `.env` local. El inicializador solo crea cuentas que no existen: cambiar esa variable no cambia la contraseña de una cuenta ya creada. No compartas el archivo ni sus claves.

### 3. Otra terminal para Angular

Abre una terminal nueva desde la raíz Git:

```powershell
cd .\frontend
npm ci
npm start
```

`npm ci` instala las dependencias de `package-lock.json`; no hace falta repetirlo en cada arranque si las dependencias no cambiaron. Abre [ParkUTP en Angular](http://localhost:4200/login) e inicia sesión.

**Deben estar activos ambos procesos:** Angular en 4200 y Spring Boot en 8081. Abrir Angular por sí solo no inicia la API. Usa `localhost` para coincidir con el origen CORS predeterminado.

### 4. Detener la demostración

Pulsa `Ctrl+C` en las terminales de Angular y Java. Para detener además la instancia PostgreSQL local, desde la raíz Git:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\parkutp\scripts\Detener-PostgresLocal.ps1
```

Los datos se conservan. No borres `.local-data` para solucionar errores de conexión.

### Otras modalidades

- [Backend con PostgreSQL propio, variables y compilación manual](docs/Guia-APF2.md#3-instalación-paso-a-paso).
- [Backend y PostgreSQL mediante Docker](parkutp/docs/Docker.md). Angular se ejecuta por separado, salvo que se configure otro despliegue.
- [Guía específica del frontend](frontend/README.md).

## Pantallas y permisos

| Ruta Angular | Función |
|---|---|
| `/login` | Autenticación. |
| `/sedes` | Administración de sedes. |
| `/estacionamientos` | Administración de estacionamientos. |
| `/accesos` | Gestión o consulta de accesos. |
| `/operacion` | Aforo y registro de movimientos según el rol. |
| `/historial` | Consulta con filtros. |

| Acción | ADMIN | OPERADOR | CONSULTA |
|---|:---:|:---:|:---:|
| Consultar aforo e historial | Sí | Sí | Sí |
| Consultar accesos | Sí | Sí | Sí |
| Registrar ingresos y salidas | Sí | Sí | No |
| Crear, editar o eliminar recursos | Sí | No | No |

El menú de sedes y estacionamientos se muestra a ADMIN. Sus formularios también restringen la modificación por rol. El backend aplica la autorización efectiva de cada petición. La sesión Angular se mantiene en memoria: recargar la página requiere volver a iniciar sesión.

## Arquitectura y estructura

```text
Angular → API REST → Controller → Service → Repository JPA → PostgreSQL
```

Angular reutiliza `AuthService`, el interceptor JWT y la navegación común. Spring Boot valida solicitudes y permisos; los servicios aplican reglas de negocio y transacciones; JPA/Hibernate accede a PostgreSQL.

```text
parkutp-proyweb/
├── frontend/                 Interfaz Angular
│   ├── src/app/core/         Autenticación, interceptor y URL de API
│   ├── src/app/layout/       Navegación y estructura compartida
│   ├── src/app/features/
│   │   ├── administracion/   Sedes y estacionamientos
│   │   └── gestion-estacionamientos/  Accesos, operación e historial
│   └── tests/               Recorridos automatizados del frontend
├── parkutp/                  Backend Java, Maven Wrapper y scripts
│   ├── src/main/             API, negocio, seguridad, entidades y vistas previas
│   ├── src/test/             Pruebas Java
│   ├── evidencias/           Evidencias del avance 2
│   ├── postman/              Colecciones de la API
│   └── docs/                 Documentación técnica del backend
├── docs/                     Índice, guías y evidencias del avance actual
├── postman/                  Colecciones de la API
└── README.md                 Punto de entrada al proyecto
```

### Reglas principales

- Capacidad entera entre 1 y 10000; no puede reducirse por debajo de la ocupación.
- Una zona llena rechaza ingresos; una inactiva permite salidas pendientes, pero no ingresos nuevos.
- Una placa no ingresa dos veces seguidas ni sale sin una entrada pendiente en ese estacionamiento.
- La creación de zona y accesos es transaccional; los movimientos utilizan control de concurrencia e idempotencia mediante UUID.
- Las relaciones y el historial pueden impedir una eliminación. Desactivar es una alternativa cuando corresponda.

Consulta el [modelo y la arquitectura](parkutp/docs/Modelo-y-arquitectura.md), las [consultas JPQL](parkutp/docs/JPQL-historial.md) y las [reglas y transacciones detalladas](docs/Guia-APF2.md#10-reglas-y-transacciones).

## Configuración

| Configuración | Ubicación o uso |
|---|---|
| URL de la API Angular | `frontend/src/app/core/api/api-config.ts`: `http://localhost:8081/api`. |
| `CORS_ORIGENES` | Orígenes permitidos por la API; predeterminado `http://localhost:4200`. |
| `PORT` | Puerto HTTP del backend; predeterminado 8081. |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexión cuando se utiliza PostgreSQL propio. |
| `JWT_SECRET` | Clave de firma; conservarla fuera de Git. |
| `DEMO_PASSWORD` | Contraseña inicial de cuentas nuevas de demostración. |
| `TEST_DB_URL`, `TEST_DB_USER`, `TEST_DB_PASSWORD` | Base exclusiva de integración Java. |

El `.env` no se carga automáticamente en cualquier modalidad: el iniciador de `docs/ricardo` carga las tres variables indicadas en sus instrucciones. El script Java directo requiere configurar el entorno por separado. Si cambias el puerto de la API, actualiza también su URL en Angular. Ver [configuración completa](docs/Guia-APF2.md#5-configuración).

## Verificación y pruebas

### Compilar Angular

Desde `frontend`:

```powershell
npm run build
```

La salida integrada se genera en `frontend/dist/frontend/browser/`. Las advertencias de presupuesto de estilos se distinguen de un fallo de compilación.

### Recorridos del frontend

Los scripts utilizan Playwright y un navegador instalado para ese entorno de pruebas. Playwright no está declarado como dependencia del proyecto: debe estar disponible en el entorno, o indicarse mediante `PLAYWRIGHT_MODULE`. Consulta los requisitos específicos de cada guía antes de ejecutarlos.

| Recorrido | Guía |
|---|---|
| Sedes, estacionamientos y cambios sin guardar | [Documentación de Melissa](docs/melissa/LEEME-MELISSA.md). |
| Accesos, operación, historial y persistencia tras reinicio | [Documentación de Ricardo](docs/ricardo/LEEME-RICARDO.md). |

**API simulada y API real son verificaciones distintas.** Los recorridos simulados evalúan la interfaz sin demostrar persistencia. Los reales requieren backend, credenciales y base preparados; algunos conservan registros para comprobarlos después del reinicio. Usa datos de prueba y sigue las instrucciones del recorrido elegido.

### Backend y Postman

- [Pruebas Java y JavaScript del backend](docs/Guia-APF2.md#11-pruebas-automatizadas).
- [Colecciones y procedimiento de Postman](docs/Guia-APF2.md#12-postman).
- [Condiciones y resultados históricos de APF2](parkutp/docs/Verificacion-APF2.md).

Las pruebas de integración Java deben apuntar únicamente a `parkutp_test`, porque limpian tablas. Los resultados de APF2 son evidencia de esa entrega, no una nueva ejecución de las pruebas sobre cada cambio del avance 3.

## Demostración

1. Iniciar sesión como ADMIN.
2. Crear una sede y un estacionamiento; comprobar sus accesos iniciales.
3. Editar capacidad o estado y mostrar las validaciones del formulario.
4. Registrar un ingreso y comprobar ocupados y disponibles.
5. Registrar la salida y buscar la placa en el historial.
6. Mostrar CONSULTA sin controles de registro y explicar la autorización del servidor.
7. Escribir un cambio sin guardarlo e intentar navegar: mostrar el aviso para seguir editando o salir.

## Solución de problemas

| Síntoma | Qué revisar |
|---|---|
| `node` o `npm` no se reconoce | Instalar una versión compatible, verificar PATH y abrir una terminal nueva de VS Code. |
| Angular indica que no hay conexión con el servidor | Comprobar el arranque de Spring Boot en 8081, la URL de API y CORS. |
| Puerto 4200 ocupado | Detener el Angular anterior antes de iniciar otro. |
| PowerShell bloquea el iniciador | Usar el comando documentado con `powershell -NoProfile -ExecutionPolicy Bypass -File`. |
| No se detecta Java | Definir `JAVA_HOME` apuntando al JDK 21 completo. |
| El login falla después de cambiar `DEMO_PASSWORD` | Las cuentas existentes conservan la contraseña con la que se crearon. |
| La página vuelve al login al recargar | La sesión Angular está en memoria; iniciar sesión de nuevo. |
| API devuelve 401 / 403 | Revisar sesión y token / permisos del rol, respectivamente. |
| No se permite eliminar un recurso | Revisar dependencias o historial; leer el mensaje de la API. |

Para problemas de PostgreSQL, WAR y pruebas, consulta el [diagnóstico del backend](docs/Guia-APF2.md#15-solución-de-problemas).

## Documentación y equipo

- [Índice de documentación](docs/README.md).
- [Sedes y estacionamientos: implementación y evidencias](docs/melissa/LEEME-MELISSA.md).
- [Accesos, operación e historial: implementación y evidencias](docs/ricardo/LEEME-RICARDO.md).
- [Guía histórica de instalación y verificación APF2](docs/Guia-APF2.md).

| Integrante | Módulos del avance Angular |
|---|---|
| Sergio | Base de la aplicación, autenticación, diseño general e integración. |
| Melissa | Sedes, estacionamientos, validaciones y protección de cambios sin guardar. |
| Ricardo | Accesos, operación, historial y sus recorridos de verificación. |

Para traer cambios de `main` a una rama personal, primero guarda el trabajo local y comprueba `git status`. Con el directorio limpio y estando en tu rama:

```powershell
git fetch origin
git merge origin/main
```

Revisa y prueba antes de hacer commit o push. Coordina la integración con el equipo. No subas `.env`, tokens, contraseñas, bases locales, `node_modules` ni archivos generados.

## Límites actuales

No se incluyen reservas, integración con la app UTP, detección automática mediante sensores físicos ni administración completa de cuentas. Son posibilidades de evolución. El funcionamiento de la aplicación no demuestra por sí mismo una reducción de congestión: esa evaluación requiere uso y medición en el entorno universitario.
