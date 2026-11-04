# Ejecutamos ParkUTP con Docker

Empaquetamos el WAR con Java 21 y lo ejecutamos junto con PostgreSQL 18. Para esta modalidad solo necesitamos Docker Desktop con motor Linux y Docker Compose: Java, Maven y PostgreSQL se ejecutan dentro de los contenedores.

## 1. Preparar la configuración

Abrimos Docker Desktop y esperamos a que su motor esté activo. En **PowerShell**, desde la raíz del repositorio (donde está `compose.yaml`):

```powershell
cd C:\Users\Sergi\Downloads\parkutp
docker version
docker compose version
.\parkutp\scripts\Configurar-Docker.ps1
```

El script pide la contraseña inicial de las cuentas **admin**, **operador** y **consulta**. Genera claves independientes y aleatorias para PostgreSQL y JWT, y guarda la configuración en `.env`, excluido de Git y del contexto de construcción. Conservamos ese archivo para los siguientes arranques. Si ya existe, el script no lo reemplaza.

Si acabamos de instalar Docker y la terminal no reconoce `docker`, abrimos una terminal nueva. En esta computadora también podemos agregar su ubicación para la sesión actual:

```powershell
$env:Path += ';C:\Users\Sergi\AppData\Local\Programs\DockerDesktop\resources\bin'
```

Como alternativa, copiamos `.env.example` a `.env` y completamos los tres valores vacíos. `JWT_SECRET` debe contener al menos 32 caracteres ASCII. No publicamos `.env` ni la salida de `docker compose config` con sus valores.

## 2. Construir e iniciar

```powershell
docker compose config --quiet
docker compose up -d --build --wait --wait-timeout 240
docker compose ps
```

La primera construcción descarga imágenes y dependencias y puede tardar varios minutos. Abrimos **http://localhost:8081/login** y usamos una de las tres cuentas con la contraseña elegida.

Si 8081 está ocupado, cambiamos `WEB_PORT=8082` en `.env` y repetimos `docker compose up -d`. Abrimos entonces http://localhost:8082/login. El puerto interno sigue siendo 8081.

La web se publica solo en esta computadora. PostgreSQL se comunica con la aplicación por `db:5432` en la red interna de Compose y no publica un puerto en Windows. Esta base es independiente del PostgreSQL instalado localmente.

## 3. Verificar y consultar errores

```powershell
docker compose ps
docker compose logs --tail 100 app
docker compose logs --tail 100 db
```

Ambos servicios deben aparecer como `healthy`. El control de la base comprueba su disponibilidad y el de la aplicación solicita `/login`. Comprobamos después el login, las pantallas de sedes y estacionamientos y las restricciones de cada rol.

La imagen se construye en dos etapas: Maven compila el WAR con JDK 21 y la imagen final contiene JRE 21. La aplicación se ejecuta con un usuario sin privilegios y dispone de una carpeta de Tomcat con permiso de escritura. La construcción omite la ejecución de pruebas, que verificamos por separado con PostgreSQL dedicado antes de empaquetar.

## 4. Detener, continuar y actualizar

```powershell
# Detener sin borrar los datos
docker compose down

# Volver a iniciar con los mismos datos
docker compose up -d --wait --wait-timeout 240

# Reconstruir después de cambiar el código
docker compose up -d --build --wait --wait-timeout 240
```

El volumen `postgres_data` conserva usuarios, sedes y movimientos al eliminar o reconstruir contenedores. No usamos `down -v` para una parada normal: esa opción elimina el volumen y sus datos.

Las cuentas demo se crean únicamente si no existen. Cambiar `DEMO_PASSWORD` en `.env` no cambia las contraseñas de cuentas ya guardadas. Cambiar `DB_PASSWORD` tampoco actualiza automáticamente la contraseña dentro de un volumen PostgreSQL existente; conservamos el valor original o realizamos una rotación explícita en la base. Cambiar `JWT_SECRET` invalida los tokens emitidos con la clave anterior.

## 5. Alcance

Esta configuración prepara una demostración local reproducible. Para publicar en Internet necesitamos un dominio, HTTPS, copias de seguridad y gestión de usuarios y credenciales del entorno de destino. Las pruebas locales del WAR no sustituyen la construcción y el arranque efectivos de la imagen.

## 6. Verificación realizada el 3 de noviembre de 2026

| Comprobación | Resultado |
|---|---|
| Pruebas previas del proyecto | 72 Java y 10 JavaScript aprobadas, sin omisiones |
| Construcción de la imagen Linux con Java 21 | Correcta |
| Inicio de PostgreSQL 18 y aplicación con Compose | Ambos servicios `healthy` |
| Login JWT y web, JSP, JSF y puente de token | Correctos para admin, operador y consulta |
| API sin token y escrituras sin permiso | Respuestas 401 y 403 esperadas |
| Crear sede y estacionamiento, registrar entrada y salida | Correcto; aforo final cero |
| Eliminar y recrear contenedores conservando volumen | Estacionamiento y dos movimientos conservados |
| Contraseña ficticia con comilla, dólar, numeral y barra invertida | Recibida sin alteraciones dentro del contenedor |

Realizamos estas comprobaciones con un proyecto Compose aislado (`parkutp-verificacion`), puerto 8082, claves temporales y base propia. Detuvimos esa instancia al terminar. Para el uso normal seguimos los pasos anteriores, con nuestras propias credenciales en `.env`.

Referencias: [dependencias y controles de salud en Compose](https://docs.docker.com/reference/compose-file/services/), [imagen oficial de PostgreSQL](https://hub.docker.com/_/postgres).
