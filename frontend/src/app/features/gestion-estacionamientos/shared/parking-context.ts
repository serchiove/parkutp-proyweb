import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { ParkingApiService } from '../data/parking-api.service';
import { Estacionamiento } from '../data/parking.models';
import { mensajeError } from './api-error';

// Una instancia por pantalla: no modifica ni duplica el estado de autenticación.
@Injectable()
export class ParkingContext {
  readonly api = inject(ParkingApiService);
  readonly auth = inject(AuthService);
  readonly estacionamientos = signal<Estacionamiento[]>([]);
  readonly seleccionado = signal<Estacionamiento | null>(null);
  readonly cargando = signal(false);
  readonly error = signal('');
  readonly esAdmin = computed(() => this.auth.session()?.rol === 'ADMIN');
  readonly puedeOperar = computed(() => ['ADMIN', 'OPERADOR'].includes(this.auth.session()?.rol ?? ''));
  readonly disponibles = computed(() => Math.max(0, (this.seleccionado()?.capacidad ?? 0) - (this.seleccionado()?.ocupados ?? 0)));
  readonly porcentaje = computed(() => {
    const e = this.seleccionado();
    return e && e.capacidad > 0 ? Math.min(100, Math.round(e.ocupados * 100 / e.capacidad)) : 0;
  });
  async cargar(preferido?: number): Promise<void> {
    this.cargando.set(true);
    this.error.set('');
    try {
      const lista = await firstValueFrom(this.api.listarEstacionamientos());
      this.estacionamientos.set(lista);
      this.seleccionado.set(lista.find(e => e.id === preferido) ?? lista[0] ?? null);
    } catch (error) {
      this.error.set(mensajeError(error));
      this.estacionamientos.set([]);
      this.seleccionado.set(null);
    } finally { this.cargando.set(false); }
  }
  seleccionar(id: number): void {
    this.seleccionado.set(this.estacionamientos().find(e => e.id === id) ?? null);
  }
  async actualizarAforo(): Promise<void> {
    const id = this.seleccionado()?.id;
    if (!id) return;
    const actualizado = await firstValueFrom(this.api.buscarEstacionamiento(id));
    if (this.seleccionado()?.id === id) {
      this.seleccionado.set(actualizado);
      this.estacionamientos.update(lista => lista.map(e => e.id === id ? actualizado : e));
    }
  }
}
