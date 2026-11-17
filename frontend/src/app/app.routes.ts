import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { AppShell } from './layout/app-shell/app-shell';
import { LoginPage } from './pages/login/login-page';
import { ModulePlaceholderPage } from './pages/module-placeholder/module-placeholder';

export const routes: Routes = [
  { path: 'login', component: LoginPage },
  {
    path: '',
    component: AppShell,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'estacionamientos' },
      { path: 'estacionamientos', component: ModulePlaceholderPage, data: { title: 'Estacionamientos', description: 'Consulta y administra los estacionamientos registrados en ParkUTP.', icon: 'P' } },
      { path: 'sedes', component: ModulePlaceholderPage, data: { title: 'Sedes', description: 'Organiza las sedes universitarias y sus espacios disponibles.', icon: 'S' } },
      { path: 'operacion', component: ModulePlaceholderPage, data: { title: 'Operación', description: 'Registra ingresos y salidas de vehículos en el estacionamiento.', icon: 'O' } },
      { path: 'historial', component: ModulePlaceholderPage, data: { title: 'Historial', description: 'Revisa los movimientos y la actividad reciente del sistema.', icon: 'H' } },
    ],
  },
  { path: '**', redirectTo: '' },
];
