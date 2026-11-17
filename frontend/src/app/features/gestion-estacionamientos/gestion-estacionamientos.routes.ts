import { Routes } from '@angular/router';

// Estas rutas se cargan dentro del AppShell protegido por authGuard.
export const gestionEstacionamientoRoutes: Routes = [
  { path: 'accesos', loadComponent: () => import('./accesos/accesos-page').then(m => m.AccesosPage) },
  { path: 'operacion', loadComponent: () => import('./operacion/operacion-page').then(m => m.OperacionPage) },
  { path: 'historial', loadComponent: () => import('./historial/historial-page').then(m => m.HistorialPage) },
];
