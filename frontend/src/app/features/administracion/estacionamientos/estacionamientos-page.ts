import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin, map } from 'rxjs';
import { Estacionamiento } from '../../gestion-estacionamientos/data/parking.models';
import { AdministracionApiService, Sede } from '../data/administracion-api.service';
import { AvisoCambios } from '../shared/aviso-cambios';
import { CrudPage } from '../shared/crud-page';

@Component({
  selector: 'app-estacionamientos-page', imports: [ReactiveFormsModule, AvisoCambios],
  templateUrl: './estacionamientos-page.html', styleUrl: '../shared/administracion.scss',
})
export class EstacionamientosPage extends CrudPage<Estacionamiento> implements OnInit {
  private readonly api = inject(AdministracionApiService);
  private readonly fb = inject(FormBuilder);
  readonly sedes = signal<Sede[]>([]);
  readonly form = this.fb.nonNullable.group({
    sedeId: ['', Validators.required],
    nombre: ['', [Validators.required, Validators.maxLength(120), Validators.pattern(/\S/)]],
    capacidad: [1, [Validators.required, Validators.min(1), Validators.max(10000), Validators.pattern(/^\d+$/)]],
    activo: [true],
  });
  ngOnInit(): void { void this.cargar(); }
  protected listar() {
    return forkJoin({ sedes: this.api.listarSedes(), zonas: this.api.listarEstacionamientos() }).pipe(
      map(({ sedes, zonas }) => { this.sedes.set(sedes); return zonas; }),
    );
  }
  protected borrar(zona: Estacionamiento) { return this.api.eliminarEstacionamiento(zona.id); }
  protected reiniciarFormulario(): void { this.form.reset({ sedeId: '', nombre: '', capacidad: 1, activo: true }); }
  protected llenarFormulario(zona: Estacionamiento): void {
    this.form.reset({ sedeId: zona.sedeId, nombre: zona.nombre, capacidad: zona.capacidad, activo: zona.activo });
  }
  async guardar(): Promise<void> {
    this.form.controls.nombre.setValue(this.form.controls.nombre.value.trim());
    await this.persistir(() => {
      const data = this.form.getRawValue();
      const zona = this.editando();
      return zona ? this.api.actualizarEstacionamiento(zona.id, data) : this.api.crearEstacionamiento(data);
    });
  }
}
