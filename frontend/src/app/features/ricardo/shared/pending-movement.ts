import { inject, Injectable, signal } from '@angular/core';
import { AuthService } from '../../../core/auth/auth.service';
import { MovimientoRequest } from '../data/parking.models';

interface EnvioPendiente { id: number; data: MovimientoRequest; }

@Injectable()
export class PendingMovement {
  private readonly auth = inject(AuthService);
  private readonly key = `parkutp.movimiento.pendiente.${this.auth.session()?.usuario ?? ''}`;
  private readonly state = signal<EnvioPendiente | null>(this.restaurar());
  readonly pendiente = this.state.asReadonly();

  actualizar(value: EnvioPendiente | null): void {
    this.state.set(value);
    try {
      if (value) sessionStorage.setItem(this.key, JSON.stringify(value));
      else sessionStorage.removeItem(this.key);
    } catch { /* Si el navegador bloquea storage, se conserva el reintento en memoria. */ }
  }
  private restaurar(): EnvioPendiente | null {
    try {
      const value = JSON.parse(sessionStorage.getItem(this.key) ?? 'null');
      if (value && Number.isSafeInteger(value.id) && value.id > 0
        && /^[0-9a-f-]{36}$/i.test(value.data?.eventoId)
        && ['ENTRADA', 'SALIDA'].includes(value.data?.tipo)
        && value.data?.origen === 'MANUAL'
        && (value.data?.placa === null || /^[A-Z0-9-]{3,12}$/.test(value.data?.placa))) return value;
    } catch { /* Una copia dañada no se envía a la API. */ }
    return null;
  }
}
