import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
@Component({ imports: [RouterLink, RouterLinkActive, RouterOutlet], selector: 'app-shell', styleUrl: './app-shell.scss', templateUrl: './app-shell.html' })
export class AppShell {
  readonly auth = inject(AuthService);
  readonly navigation = [
    { label: 'Estacionamientos', path: '/estacionamientos', icon: '▦' },
    { label: 'Sedes', path: '/sedes', icon: '⌖' },
    { label: 'Accesos', path: '/accesos', icon: '⇄' },
    { label: 'Operación', path: '/operacion', icon: '↔' },
    { label: 'Historial', path: '/historial', icon: '◷' },
  ];
}
