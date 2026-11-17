import { Routes } from '@angular/router';

// El coordinador puede incorporar estas rutas como hijas del AppShell protegido.
export const ricardoRoutes: Routes = [
  { path: 'accesos', loadComponent: () => import('./accesos/accesos-page').then(m => m.AccesosPage) },
  { path: 'operacion', loadComponent: () => import('./operacion/operacion-page').then(m => m.OperacionPage) },
  { path: 'historial', loadComponent: () => import('./historial/historial-page').then(m => m.HistorialPage) },
];
