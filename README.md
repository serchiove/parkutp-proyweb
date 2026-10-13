<div align="center">

# ParkUTP

### Gestión de estacionamientos · UTP Arequipa

**Proyecto Integrador — Desarrollo Web Integrado**<br>
**Avance 2 · Persistencia y seguridad**

</div>

<p align="center">
  <img alt="Entrega APF2" src="https://img.shields.io/badge/Entrega-APF2-B91C1C?style=flat-square" />
  <img alt="Java 21" src="https://img.shields.io/badge/Java-21-2563EB?style=flat-square" />
  <img alt="Spring Boot 3.2.5" src="https://img.shields.io/badge/Spring_Boot-3.2.5-15803D?style=flat-square" />
  <img alt="PostgreSQL" src="https://img.shields.io/badge/Base_de_datos-PostgreSQL-334155?style=flat-square" />
  <img alt="Seguridad JWT" src="https://img.shields.io/badge/Seguridad-JWT-7C3AED?style=flat-square" />
</p>

Desarrollamos **ParkUTP** para administrar sedes y estacionamientos, registrar entradas y salidas y consultar aforo e historial. En este avance integramos una base PostgreSQL, persistencia JPA/Hibernate y seguridad con roles y JWT, manteniendo una interfaz web con JSP, JSF y Bootstrap.

| El problema que abordamos | Nuestra solución | Nuestro equipo |
|:---|:---|:---|
| Centralizar el control de disponibilidad y movimientos por zona. | Administración, operación y consulta con datos persistentes y permisos diferenciados. | **Sergio · Melissa · Ricardo** |

> [!NOTE]
> Presentamos la implementación del **APF2**. Angular, reservas e integración con dispositivos físicos corresponden a etapas posteriores.

## Recorrido por nuestro proyecto

| Área | Qué implementamos | Documentación |
|:---|:---|:---|
| Administración | Sedes, zonas y accesos. | [Guía de uso](parkutp/README.md#7-guía-de-uso) |
| Operación | Entradas/salidas, aforo y control por placa. | [Reglas y transacciones](parkutp/README.md#10-reglas-y-transacciones) |
| Seguridad | ADMIN, OPERADOR, CONSULTA, JWT y sesión web. | [Usuarios y seguridad](parkutp/README.md#6-usuarios-y-seguridad) |
| Persistencia | PostgreSQL, entidades JPA y consultas JPQL. | [Arquitectura y datos](parkutp/README.md#8-arquitectura-y-datos) |
| Verificación | Pruebas automatizadas y colecciones autenticadas. | [Pruebas](parkutp/README.md#11-pruebas-automatizadas) · [Postman](parkutp/README.md#12-postman) |

## Ejecutar nuestra aplicación

| Paso | Acción |
|:---:|:---|
| **1** | Clonar el repositorio y entrar al proyecto Maven en `parkutp/`. |
| **2** | Configurar JDK 21 y PostgreSQL. |
| **3** | Introducir las claves en PowerShell mediante Read-Host. |
| **4** | Ejecutar `scripts/Iniciar.ps1` y abrir `http://localhost:8081/login`. |

**[Abrir la instalación completa paso a paso →](parkutp/README.md#3-instalación-paso-a-paso)**

> [!IMPORTANT]
> El proyecto Maven está en la subcarpeta `parkutp/`. Los scripts y Maven se ejecutan desde allí. Configuramos las credenciales en el entorno; el clon descarga código, no datos PostgreSQL ni contraseñas.

## Resultados de verificación

| Java | JavaScript | Solicitudes de colecciones | Aserciones de colecciones |
|:---:|:---:|:---:|:---:|
| **72 aprobadas** | **10 aprobadas** | **32 verificadas** | **38 aprobadas** |

Los resultados corresponden al cierre técnico documentado de nuestro APF2. Condiciones y metodología en [Verificación APF2](parkutp/docs/Verificacion-APF2.md).

## Documentación y presentación

| Consultar | Acceso |
|:---|:---|
| Manual completo | [Instalación, uso, arquitectura, API y pruebas](parkutp/README.md) |
| Contrato REST | [Endpoints, ejemplos y códigos HTTP](parkutp/README.md#9-api-rest) |
| Exposición | [Guion de nuestra demostración](parkutp/README.md#14-demostración-del-apf2) |
| Diagnóstico | [Errores frecuentes](parkutp/README.md#15-solución-de-problemas) |
| Requisitos del avance | [Matriz de cumplimiento](parkutp/README.md#17-cumplimiento-y-documentación) |
| Propuestas futuras | [Límites y evolución](parkutp/README.md#18-límites-y-evolución) |

---

**Equipo:** Sergio, Melissa y Ricardo · **Proyecto:** ParkUTP · **Entrega:** Avance 2
