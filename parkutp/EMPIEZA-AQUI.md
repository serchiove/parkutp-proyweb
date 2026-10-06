# Empieza aquí — ParkUTP APF2

Esta es una versión funcional para preparar el avance 2. Incluye el CRUD de estacionamientos, base de datos PostgreSQL con JDBC, pantallas JSP con Bootstrap y el formulario JSF para registrar sedes. También incluye movimientos, aforo e indicadores virtuales como ampliación.

## 1. Abrir el proyecto

Descomprime el ZIP. La carpeta `ParkUTP-APF2` es el proyecto; su archivo `pom.xml` permite abrirlo como proyecto Maven en tu IDE.

Necesitas JDK 21 y PostgreSQL. En esta computadora se encontraron ambos instalados. La primera compilación descarga dependencias desde Internet.

## 2. Ejecutar

Abre una terminal PowerShell dentro de `ParkUTP-APF2` y ejecuta:

```powershell
.\scripts\Iniciar.ps1 -DemoLocal
```

Si no detecta el JDK, configura antes `JAVA_HOME` con la ruta de tu JDK 21. El script prepara una base de datos local independiente y luego inicia la aplicación. Espera el mensaje de que Tomcat inició.

Abre **http://localhost:8081** en tu navegador.

## 3. Probar el sistema

1. Entra a **Sedes** y registra un código, por ejemplo `AQP-PARRA`, y un nombre.
2. Vuelve a **Estacionamientos** y pulsa **Nuevo estacionamiento**.
3. Selecciona la sede, escribe el nombre de la zona y una capacidad de prueba, por ejemplo 20.
4. Comprueba que puedes editarla. Para demostrar eliminar, crea otra zona temporal y elimínala antes de registrar movimientos.
5. En la zona principal, entra a **Ver operación**.
6. Registra una entrada: quedan 19 disponibles. Registra una salida: vuelven a 20.
7. Abre esa pantalla en dos ventanas para mostrar la actualización compartida cada 5 segundos.

La placa es opcional. Esta versión cuenta vehículos; todavía no empareja visitas por placa. El simulador y la luz son virtuales, sin sensores físicos conectados.

## 4. Dónde mirar para explicar el código

| Tema | Archivo o carpeta |
|---|---|
| Tablas y relaciones | `src/main/resources/schema.sql` |
| Formularios y endpoints | `controller/web` y `controller/api` |
| Validaciones de entrada | `dto` |
| Reglas de negocio y transacciones | `service` |
| JDBC y consultas SQL | `dao` |
| Pantallas JSP | `src/main/webapp/WEB-INF/views` |
| Formulario JSF | `src/main/webapp/sedes.xhtml` y `view/SedeBean.java` |
| Pruebas | `src/test/java` |
| Modelo y siguiente etapa | `docs/Modelo-y-arquitectura.md` |
| Guion para presentar | `docs/Guion-demo.md` |

## 5. Conservar el mismo repositorio

El ZIP es una evolución del proyecto que compartiste. No hace falta crear otro repositorio para APF2. Antes de integrar los archivos, guarda un commit del APF1 y conserva la carpeta `.git` de tu repositorio. Usa esta estructura como reemplazo de la versión de código anterior; mezclar ambas carpetas `src` puede registrar dos controllers con las mismas rutas y dos servicios con el mismo nombre.

Las evidencias del primer avance se conservan. Para APF2, genera capturas nuevas de las pantallas y pruebas de esta versión.

## 6. Detener

Ctrl+C en la terminal detiene la aplicación. Después, para detener la base local de demostración:

```powershell
.\scripts\Detener-PostgresLocal.ps1
```

Los datos se conservan para la próxima ejecución. En `README.md` están la configuración con una base existente, la API, las pruebas y los límites del prototipo.
