# ParkUTP

## Descripción

ParkUTP es un proyecto orientado al monitoreo de la disponibilidad de estacionamientos de la Universidad Tecnológica del Perú (UTP).

La propuesta busca facilitar a estudiantes y docentes la consulta de espacios disponibles en los estacionamientos considerados, mediante el desarrollo progresivo de una plataforma web.

En el APF1 se desarrolla una API REST utilizando Spring Boot, aplicando arquitectura por capas, inyección de dependencias, CRUD en memoria y pruebas automatizadas.

---

## Usuarios

Los principales usuarios considerados son:

- Estudiantes de la UTP que utilizan vehículo.
- Docentes de la UTP que utilizan vehículo.

---

## Alcance del APF1

El alcance conceptual de ParkUTP considera tres recursos principales:

- **Sede:** representa las sedes consideradas por el sistema.
- **Estacionamiento:** representa las zonas de estacionamiento asociadas a cada sede.
- **Movimiento:** representa los ingresos y salidas de vehículos.

Para el APF1 se implementa el CRUD en memoria únicamente del recurso **Sede**. 
Los recursos **Estacionamiento** y **Movimiento** forman parte del diseño del sistema 
y serán desarrollados progresivamente en las siguientes etapas del proyecto.

---

## Tecnologías utilizadas

- Java 21
- Spring Boot 3.2.5
- Maven
- JUnit 5
- MockMvc
- API REST

---

## Arquitectura

La aplicación utiliza una arquitectura organizada en tres capas:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Almacenamiento en memoria
```

### Controller

Gestiona las solicitudes HTTP y devuelve las respuestas con los códigos HTTP correspondientes.

### Service

Contiene la lógica de negocio y comunica el Controller con el Repository.

### Repository

Gestiona temporalmente los datos mediante una lista en memoria.

La comunicación entre las capas se realiza mediante inyección de dependencias por constructor.

---

## Matriz de endpoints REST

### Recurso Sede - Implementado en APF1

| Método |     Endpoint      |         Descripción               | Respuesta exitosa | Posible error |
|--------|-------------------|-----------------------------------|-------------------|---------------|
|  GET   | `/api/sedes`      | Lista todas las sedes registradas | 200 OK            | -             |
|  GET   | `/api/sedes/{id}` | Obtiene una sede por su ID        | 200 OK            | 404 Not Found |
|  POST  | `/api/sedes`      | Registra una nueva sede           | 201 Created       | -             |
|  PUT   | `/api/sedes/{id}` | Actualiza una sede existente      | 200 OK            | 404 Not Found |
| DELETE | `/api/sedes/{id}` | Elimina una sede por su ID        | 204 No Content    | 404 Not Found |

### Recursos propuestos para etapas posteriores

|     Recurso     | Método |           Endpoint           |              Descripción             | Estado    |
|-----------------|--------|------------------------------|--------------------------------------|-----------|
| Estacionamiento | GET    | `/api/estacionamientos`      | Lista los estacionamientos           | Propuesto |
| Estacionamiento | GET    | `/api/estacionamientos/{id}` | Consulta un estacionamiento por ID   | Propuesto |
| Movimiento      | GET    | `/api/movimientos`           | Consulta los movimientos registrados | Propuesto |
| Movimiento      | POST   | `/api/movimientos`           | Registra un ingreso o salida         | Propuesto |

---

## Ejemplo de Sede

```json
{
  "id": "1",
  "nombre": "UTP Arequipa - Sede Parra"
}
```

---

## Ejecución del proyecto

### Requisitos

Para ejecutar el proyecto se requiere:

- Java 21
- Maven Wrapper incluido en el proyecto

### Windows

Desde una terminal ubicada en la carpeta `parkutp`, ejecutar:

```powershell
.\mvnw.cmd spring-boot:run
```

Una vez iniciada la aplicación, la API estará disponible en:

```text
http://localhost:8081
```

El endpoint principal del recurso Sede es:

```text
http://localhost:8081/api/sedes
```

---

## Ejecución de pruebas

Para ejecutar las pruebas automatizadas:

```powershell
.\mvnw.cmd test
```

Actualmente el proyecto cuenta con pruebas de Service y pruebas mediante MockMvc.

---

## Pruebas de la API

El proyecto incluye una colección de Postman denominada
`APF1-ParkUTP.postman_collection.json`, utilizada para comprobar
el funcionamiento de los endpoints de la API y sus códigos de respuesta HTTP.

Los casos comprobados incluyen:

- `GET /api/sedes` → `200 OK`
- `GET /api/sedes/{id}` → `200 OK` o `404 Not Found`
- `POST /api/sedes` → `201 Created`
- `PUT /api/sedes/{id}` → `200 OK` o `404 Not Found`
- `DELETE /api/sedes/{id}` → `204 No Content` o `404 Not Found`

La colección incluye un caso `4xx` mediante la consulta de una sede inexistente,
que retorna `404 Not Found`.

---

## Limitaciones del APF1

En esta primera etapa:

- Los datos se almacenan únicamente en memoria.
- No se utiliza una base de datos.
- No se implementa autenticación JWT ni gestión de roles.
- No se utiliza Angular.
- No existe integración con servicios cloud.
- Los sensores para detectar ingresos y salidas de vehículos forman parte de etapas posteriores del proyecto.