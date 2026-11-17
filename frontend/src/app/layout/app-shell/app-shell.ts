import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
@Component({ imports: [RouterLink, RouterLinkActive, RouterOutlet], selector: 'app-shell', styleUrl: './app-shell.scss', templateUrl: './app-shell.html' })
export class AppShell {
  readonly auth = inject(AuthService);
  readonly navigation = computed(() => [
    { label: 'Estacionamientos · pendiente', path: '/estacionamientos', icon: '▦' },
    { label: 'Sedes', path: '/sedes', icon: '⌖' },
    { label: this.auth.session()?.rol === 'ADMIN' ? 'Gestionar accesos' : 'Consultar accesos', path: '/accesos', icon: '⇄' },
    { label: this.auth.session()?.rol === 'CONSULTA' ? 'Aforo' : 'Operación', path: '/operacion', icon: '↔' },
    { label: 'Historial', path: '/historial', icon: '◷' },
  ].filter(item => this.auth.session()?.rol === 'ADMIN' || !['/sedes', '/estacionamientos'].includes(item.path)));
}

