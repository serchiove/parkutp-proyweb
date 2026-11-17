import { Routes } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './core/auth/auth.service';
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
      { path: '', pathMatch: 'full', redirectTo: () => inject(AuthService).homePath() },
      { path: 'estacionamientos', component: ModulePlaceholderPage, data: { title: 'Estacionamientos', description: 'Consulta y administra los estacionamientos registrados en ParkUTP.', icon: 'P' } },
      { path: 'sedes', loadComponent: () => import('./features/administracion/sedes/sedes-page').then(m => m.SedesPage) },
      ...gestionEstacionamientoRoutes,
    ],
  },
  { path: '**', redirectTo: '' },
];
