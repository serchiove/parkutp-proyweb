import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { FiltrosHistorial, Movimiento, TipoMovimiento } from '../data/parking.models';
import { mensajeError } from '../shared/api-error';
import { ParkingContext } from '../shared/parking-context';
import { ParkingSelector } from '../shared/parking-selector';

@Component({
  selector: 'app-historial-page',
  imports: [ReactiveFormsModule, ParkingSelector],
  providers: [ParkingContext],
  templateUrl: './historial-page.html',
  styleUrl: '../shared/gestion-estacionamientos.scss',
})
export class HistorialPage {
  readonly ctx = inject(ParkingContext);
  private readonly fb = inject(FormBuilder);
  readonly movimientos = signal<Movimiento[]>([]);
  readonly cargando = signal(false);
  readonly error = signal('');
  private consulta = 0;
  private aplicados: FiltrosHistorial = { fecha: '', placa: '', tipo: '' };
  readonly form = this.fb.nonNullable.group({
    fecha: [''],
    placa: ['', Validators.maxLength(12)],
    tipo: ['' as TipoMovimiento | ''],
  });
  cambiarEstacionamiento(): void { void this.cargar(); }
  buscar(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    this.aplicados = this.form.getRawValue();
    void this.cargar();
  }
  limpiar(): void {
    this.form.reset({ fecha: '', placa: '', tipo: '' });
    this.aplicados = this.form.getRawValue();
    void this.cargar();
  }
  async cargar(): Promise<void> {
    const version = ++this.consulta;
    const id = this.ctx.seleccionado()?.id;
    this.error.set(''); this.movimientos.set([]); this.cargando.set(true);
    try {
      if (id) {
        const lista = await firstValueFrom(this.ctx.api.listarMovimientos(id, this.aplicados));
        if (version === this.consulta) this.movimientos.set(lista);
      }
    } catch (error) {
      if (version === this.consulta) this.error.set(mensajeError(error));
    } finally { if (version === this.consulta) this.cargando.set(false); }
  }
  fechaLima(valor: string): string {
    return new Intl.DateTimeFormat('es-PE', {
      timeZone: 'America/Lima', dateStyle: 'short', timeStyle: 'medium',
    }).format(new Date(valor));
  }
}
