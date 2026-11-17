import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { TipoMovimiento } from '../data/parking.models';
import { mensajeError } from '../shared/api-error';
import { ParkingContext } from '../shared/parking-context';
import { ParkingSelector } from '../shared/parking-selector';
import { PendingMovement } from '../shared/pending-movement';

@Component({
  selector: 'app-operacion-page',
  imports: [ReactiveFormsModule, ParkingSelector],
  providers: [ParkingContext, PendingMovement],
  templateUrl: './operacion-page.html',
  styleUrl: '../shared/ricardo.scss',
})
export class OperacionPage {
  readonly ctx = inject(ParkingContext);
  private readonly fb = inject(FormBuilder);
  readonly guardando = signal(false);
  readonly actualizando = signal(false);
  readonly error = signal('');
  readonly exito = signal('');
  readonly aviso = signal('');
  // Se conserva un envío incierto para reintentar el mismo evento sin duplicar el aforo.
  private readonly envio = inject(PendingMovement);
  readonly pendiente = this.envio.pendiente;
  readonly form = this.fb.nonNullable.group({
    tipo: ['ENTRADA' as TipoMovimiento, Validators.required],
    placa: ['', Validators.pattern(/^[A-Za-z0-9-]{3,12}$/)],
  });
  cambiarEstacionamiento(): void {
    this.error.set(''); this.exito.set(''); this.aviso.set('');
    this.form.reset({ tipo: 'ENTRADA', placa: '' });
  }
  async actualizar(): Promise<void> {
    if (this.actualizando() || this.guardando()) return;
    this.actualizando.set(true); this.aviso.set('');
    try { await this.ctx.actualizarAforo(); }
    catch (error) { this.aviso.set(`No se pudo actualizar el aforo. ${mensajeError(error)}`); }
    finally { this.actualizando.set(false); }
  }
  async registrar(): Promise<void> {
    if (this.guardando() || this.actualizando() || this.pendiente() || !this.ctx.puedeOperar()) return;
    this.form.controls.placa.setValue(this.form.controls.placa.value.trim().toUpperCase());
    this.form.markAllAsTouched();
    const id = this.ctx.seleccionado()?.id;
    if (!id || this.form.invalid) return;
    const values = this.form.getRawValue();
    this.envio.actualizar({ id, data: { eventoId: crypto.randomUUID(), tipo: values.tipo, origen: 'MANUAL', placa: values.placa || null } });
    await this.enviar();
  }
  async enviar(): Promise<void> {
    const pendiente = this.pendiente();
    if (!pendiente || this.guardando() || !this.ctx.puedeOperar()) return;
    this.guardando.set(true); this.error.set(''); this.exito.set(''); this.aviso.set('');
    try {
      const movimiento = await firstValueFrom(this.ctx.api.registrarMovimiento(pendiente.id, pendiente.data));
      this.envio.actualizar(null);
      this.exito.set(`${movimiento.tipo === 'ENTRADA' ? 'Ingreso registrado' : 'Salida registrada'}: ${movimiento.placa ?? 'sin placa'}. Movimiento #${movimiento.id}.`);
      this.form.controls.placa.reset('');
      try { await this.ctx.actualizarAforo(); }
      catch (error) { this.aviso.set(`El movimiento fue registrado, pero el aforo no se pudo actualizar. ${mensajeError(error)}`); }
    } catch (error) {
      this.error.set(mensajeError(error));
      // Un error de red o servidor puede llegar después de que el servidor guardó el evento.
      const status = (error as { status?: number })?.status;
      if (status !== 0 && status !== undefined && status < 500) this.envio.actualizar(null);
    } finally { this.guardando.set(false); }
  }
}
