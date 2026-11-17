import { provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter, Routes, withComponentInputBinding } from '@angular/router';
import { App } from './app/app';
import { routes } from './app/app.routes';
import { authInterceptor } from './app/core/auth/auth.interceptor';
import { gestionEstacionamientoRoutes } from './app/features/gestion-estacionamientos/gestion-estacionamientos.routes';

// Entrada local de revisión. No modifica el router ni el menú del equipo.
const propias = new Set(gestionEstacionamientoRoutes.map(r => r.path));
const previewRoutes: Routes = routes.map(route => route.children
  ? { ...route, children: [...route.children.filter(child => !propias.has(child.path)), ...gestionEstacionamientoRoutes] }
  : route);

bootstrapApplication(App, {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideRouter(previewRoutes, withComponentInputBinding()),
  ],
}).catch(error => console.error(error));
