import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdministracionApiService, Sede } from '../data/administracion-api.service';
import { CrudPage } from '../shared/crud-page';

@Component({
  selector: 'app-sedes-page', imports: [ReactiveFormsModule],
  templateUrl: './sedes-page.html', styleUrl: '../shared/administracion.scss',
})
export class SedesPage extends CrudPage<Sede> implements OnInit {
  private readonly api = inject(AdministracionApiService);
  private readonly fb = inject(FormBuilder);
  readonly form = this.fb.nonNullable.group({
    id: ['', [Validators.required, Validators.pattern(/^[A-Za-z0-9_-]{1,64}$/)]],
    nombre: ['', [Validators.required, Validators.maxLength(120), Validators.pattern(/\S/)]],
  });
  ngOnInit(): void { void this.cargar(); }
  protected listar() { return this.api.listarSedes(); }
  protected borrar(sede: Sede) { return this.api.eliminarSede(sede.id); }
  protected reiniciarFormulario(): void {
    this.form.controls.id.enable(); this.form.reset({ id: '', nombre: '' });
  }
  protected llenarFormulario(sede: Sede): void {
    this.form.reset(sede); this.form.controls.id.disable();
  }
  async guardar(): Promise<void> {
    this.form.controls.id.setValue(this.form.controls.id.value.trim());
    this.form.controls.nombre.setValue(this.form.controls.nombre.value.trim());
    await this.persistir(() => {
      const data = this.form.getRawValue();
      const sede = this.editando();
      return sede ? this.api.actualizarSede(sede.id, data) : this.api.crearSede(data);
    });
  }
}
