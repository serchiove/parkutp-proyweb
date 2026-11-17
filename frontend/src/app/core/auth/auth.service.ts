import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { API_BASE_URL } from '../api/api-config';
import { AuthResponse, AuthSession, LoginRequest } from './auth.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly sessionState = signal<AuthSession | null>(null);
  readonly session = this.sessionState.asReadonly();
  readonly homePath = computed(() => this.sessionState()?.rol === 'CONSULTA' ? '/historial' : '/operacion');
  readonly isAuthenticated = computed(() => this.sessionState() !== null);

  login(credentials: LoginRequest) {
    return this.http.post<AuthResponse>(`${API_BASE_URL}/auth/login`, credentials).pipe(
      tap((response) => this.sessionState.set({
        token: response.token, tipo: response.tipo, usuario: response.usuario, rol: response.rol,
      })),
    );
  }
  logout(): void { this.sessionState.set(null); void this.router.navigate(['/login']); }
  clearSession(): void { this.sessionState.set(null); }
}
