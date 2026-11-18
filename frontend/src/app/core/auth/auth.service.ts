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
  private readonly sessionState = signal<AuthSession | null>(this.readSession());
  readonly session = this.sessionState.asReadonly();
  readonly homePath = computed(() => this.sessionState()?.rol === 'CONSULTA' ? '/historial' : '/operacion');
  readonly isAuthenticated = computed(() => this.sessionState() !== null);

  login(credentials: LoginRequest) {
    return this.http.post<AuthResponse>(`${API_BASE_URL}/auth/login`, credentials).pipe(
      tap((response) => this.sessionState.set({
        token: response.token, tipo: response.tipo, usuario: response.usuario, rol: response.rol,
      })),
      tap(session => this.saveSession({ token: session.token, tipo: session.tipo, usuario: session.usuario, rol: session.rol })),
    );
  }
  logout(): void { this.clearSession(); void this.router.navigate(['/login']); }
  clearSession(): void { this.sessionState.set(null); this.removeSession(); }

  private readSession(): AuthSession | null {
    try {
      const value: unknown = JSON.parse(sessionStorage.getItem('parkutp.auth.session') ?? 'null');
      if (!value || typeof value !== 'object') return null;
      const session = value as Partial<AuthSession>;
      return typeof session.token === 'string' && typeof session.tipo === 'string'
        && typeof session.usuario === 'string' && typeof session.rol === 'string'
        ? session as AuthSession : null;
    } catch { return null; }
  }
  private saveSession(session: AuthSession): void {
    try { sessionStorage.setItem('parkutp.auth.session', JSON.stringify(session)); } catch { /* La sesión en memoria sigue funcionando. */ }
  }
  private removeSession(): void {
    try { sessionStorage.removeItem('parkutp.auth.session'); } catch { /* Puede estar bloqueado por el navegador. */ }
  }
}
