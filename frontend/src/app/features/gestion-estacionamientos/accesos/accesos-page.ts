import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { Acceso, TipoMovimiento } from '../data/parking.models';
import { mensajeError } from '../shared/api-error';
import { ParkingContext } from '../shared/parking-context';
import { ParkingSelector } from '../shared/parking-selector';

@Component({
  selector: 'app-accesos-page',
  imports: [ReactiveFormsModule, ParkingSelector],
  providers: [ParkingContext],
  templateUrl: './accesos-page.html',
  styleUrl: '../shared/gestion-estacionamientos.scss',
})
export class AccesosPage {
  readonly ctx = inject(ParkingContext);
  private readonly fb = inject(FormBuilder);
  readonly accesos = signal<Acceso[]>([]);
  readonly cargando = signal(false);
  readonly guardando = signal(false);
  readonly error = signal('');
  readonly exito = signal('');
  readonly editando = signal<Acceso | null>(null);
  readonly porEliminar = signal<Acceso | null>(null);
  private consulta = 0;
  readonly form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(80), Validators.pattern(/\S/)]],
    tipo: ['ENTRADA' as TipoMovimiento, Validators.required],
  });

  cambiarEstacionamiento(): void {
    this.cancelar();
    this.exito.set('');
    void this.cargar();
  }
  async cargar(): Promise<void> {
    const version = ++this.consulta;
    const id = this.ctx.seleccionado()?.id;
    this.error.set('');
    this.accesos.set([]);
    this.cargando.set(true);
    try {
      if (id) {
        const lista = await firstValueFrom(this.ctx.api.listarAccesos(id));
        if (version === this.consulta) this.accesos.set(lista);
      }
    } catch (error) {
      if (version === this.consulta) this.error.set(mensajeError(error));
    } finally { if (version === this.consulta) this.cargando.set(false); }
  }
  editar(acceso: Acceso): void {
    if (!this.ctx.esAdmin() || this.guardando()) return;
    this.editando.set(acceso);
    this.porEliminar.set(null);
    this.error.set(''); this.exito.set('');
    this.form.reset({ nombre: acceso.nombre, tipo: acceso.tipo });
    this.form.controls.tipo.disable();
  }
  cancelar(): void {
    this.editando.set(null); this.porEliminar.set(null);
    this.form.controls.tipo.enable();
    this.form.reset({ nombre: '', tipo: 'ENTRADA' });
  }
  async guardar(): Promise<void> {
    if (!this.ctx.esAdmin() || this.guardando() || this.cargando()) return;
    this.form.controls.nombre.setValue(this.form.controls.nombre.value.trim());
    this.form.markAllAsTouched();
    const id = this.ctx.seleccionado()?.id;
    if (!id || this.form.invalid) return;
    const data = this.form.getRawValue();
    this.guardando.set(true); this.error.set(''); this.exito.set('');
    try {
      const editando = this.editando();
      const acceso = await firstValueFrom(editando
        ? this.ctx.api.actualizarAcceso(id, editando.id, data)
        : this.ctx.api.crearAcceso(id, data));
      this.cancelar();
      this.exito.set(`Acceso «${acceso.nombre}» ${editando ? 'actualizado' : 'creado'} correctamente.`);
      await this.cargar();
    } catch (error) { this.error.set(mensajeError(error)); }
    finally { this.guardando.set(false); }
  }
  async eliminar(): Promise<void> {
    const acceso = this.porEliminar();
    const id = this.ctx.seleccionado()?.id;
    if (!id || !acceso || !this.ctx.esAdmin() || this.guardando()) return;
    this.guardando.set(true); this.error.set(''); this.exito.set('');
    try {
      await firstValueFrom(this.ctx.api.eliminarAcceso(id, acceso.id));
      this.cancelar();
      this.exito.set(`Acceso «${acceso.nombre}» eliminado correctamente.`);
      await this.cargar();
    } catch (error) { this.error.set(mensajeError(error)); }
    finally { this.guardando.set(false); }
  }
}
