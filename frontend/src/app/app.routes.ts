import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { AppShell } from './layout/app-shell/app-shell';
import { LoginPage } from './pages/login/login-page';
import { ModulePlaceholderPage } from './pages/module-placeholder/module-placeholder';
import { gestionEstacionamientoRoutes } from './features/gestion-estacionamientos/gestion-estacionamientos.routes';

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
      ...gestionEstacionamientoRoutes,
    ],
  },
  { path: '**', redirectTo: '' },
];
