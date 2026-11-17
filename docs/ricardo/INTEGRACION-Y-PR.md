# Integración propuesta para el coordinador

## Preparar la rama de Ricardo

Ejecutar estos pasos dentro de la copia real del repositorio del equipo, después de actualizar la rama de integración acordada y comprobar que no hay trabajo local pendiente:

```powershell
git status
git switch -c ricardo/accesos-operacion-historial
```

Copiar únicamente las carpetas y archivos nuevos de esta entrega:

- `frontend/src/app/features/gestion-estacionamientos/`
- `frontend/src/main.ricardo.ts`
- `frontend/preview-ricardo.mjs`
- `frontend/serve-ricardo.mjs`
- `frontend/tests/ricardo.e2e.mjs`
- `frontend/tests/ricardo.real.e2e.mjs`
- `frontend/tests/ricardo.persistence.mjs`
- `docs/ricardo/`

No copiar encima el proyecto completo del ZIP a un checkout actualizado: podría reemplazar avances recientes de otros compañeros. No subir `node_modules`, `dist`, `.angular`, credenciales ni archivos `.env`.

Revisar `git diff --stat` y `git status`. Seleccionar explícitamente los archivos del aporte al preparar el commit, evitando agregar archivos ajenos con `git add .`.

## Cambio de rutas que debe revisar el coordinador

`frontend/src/app/app.routes.ts` original no se modificó. Para integrar:

1. Importar:

```typescript
import { gestionEstacionamientoRoutes } from './features/gestion-estacionamientos/gestion-estacionamientos.routes';
```

2. Dentro de `children` del `AppShell` protegido, retirar las dos rutas provisionales de `operacion` e `historial` y añadir:

```typescript
...gestionEstacionamientoRoutes,
```

Esto incorpora `/accesos`, `/operacion` e `/historial` bajo el `authGuard` existente. No duplicar las rutas de operación e historial: Angular toma la primera coincidencia y podría seguir mostrando el placeholder.

Conservar las rutas de Melissa, la redirección inicial, login y comodín según la versión vigente del equipo. No copiar todo el archivo de rutas de una versión anterior.

3. El menú actual ya tiene Operación e Historial. El coordinador puede decidir si agrega Accesos a la barra lateral, por ejemplo en `navigation` de `app-shell.ts`:

```typescript
{ label: 'Accesos', path: '/accesos', icon: '⇄' },
```

No es obligatorio cambiar el menú para revisar el aporte: las tres pantallas tienen pestañas internas. La ubicación definitiva de Accesos se debe acordar con el equipo.

4. Ejecutar el arranque normal con `npm start`, comprobar las rutas y compilar con `npm run build`. La entrada `main.ricardo.ts` solo se usa para revisión aislada; no sustituye `main.ts` en la integración.

## Borrador de PR

Título: **Implementar accesos, operación e historial en Angular**

Descripción sugerida:

Se reemplazarán las pantallas provisionales de operación e historial por formularios y consultas conectados a la API existente. Se añade gestión de accesos por estacionamiento con creación, edición de nombre, eliminación confirmada y permisos de ADMIN. Operación registra entradas y salidas, actualiza aforo y conserva el UUID en envíos inciertos; historial combina filtros por fecha, placa y tipo.

El aporte reutiliza AuthService, HttpClient y el interceptor JWT existentes. Los archivos actuales de sedes, estacionamientos, autenticación, backend, rutas principales y menú conservan su contenido. Se incluye una entrada independiente para revisar el avance antes de integrar las rutas.

Validación: compilación de desarrollo y producción aprobadas; 24 comprobaciones de navegador con API simulada y 14 con Spring Boot y PostgreSQL 18.7 reales aprobadas. Se comprobó persistencia después de reiniciar backend y base local, conservando cuatro movimientos y aforo cero. Las capturas reales y simuladas están en carpetas separadas. La compilación del backend aprobó 15 pruebas y omitió 57 que requieren una base dedicada; estas últimas no se reportan como ejecutadas.

Coordinación solicitada: confirmar si el coordinador incorporará `gestionEstacionamientoRoutes` bajo el AppShell protegido y si Accesos debe aparecer también en la barra lateral. No se deben conservar las rutas provisionales duplicadas de operación e historial.

## Mensajes de commit sugeridos

- `Implementar gestión de accesos por estacionamiento`
- `Agregar registro de movimientos y consulta de aforo`
- `Agregar historial con filtros y estados de consulta`
- `Documentar integración y validar recorrido de Ricardo`

Si todos los archivos se incorporan en una sola revisión, también es válido un commit de funcionalidad completo con el título del PR. El texto debe describir lo que se entrega y las pruebas que efectivamente se ejecutaron.
