import { Routes } from '@angular/router';
import { cambiosSinGuardarGuard } from './features/administracion/shared/cambios-sin-guardar.guard';
import { inject } from '@angular/core';
import { AuthService } from './core/auth/auth.service';
import { authGuard } from './core/auth/auth.guard';
import { AppShell } from './layout/app-shell/app-shell';
import { LoginPage } from './pages/login/login-page';
import { gestionEstacionamientoRoutes } from './features/gestion-estacionamientos/gestion-estacionamientos.routes';

export const routes: Routes = [
  { path: 'login', component: LoginPage },
  {
    path: '',
    component: AppShell,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: () => inject(AuthService).homePath() },
      { path: 'estacionamientos', canDeactivate: [cambiosSinGuardarGuard], loadComponent: () => import('./features/administracion/estacionamientos/estacionamientos-page').then(m => m.EstacionamientosPage) },
      { path: 'sedes', canDeactivate: [cambiosSinGuardarGuard], loadComponent: () => import('./features/administracion/sedes/sedes-page').then(m => m.SedesPage) },
      ...gestionEstacionamientoRoutes,
    ],
  },
  { path: '**', redirectTo: '' },
];
