import { computed, Directive, inject, signal } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { firstValueFrom, Observable } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { mensajeError } from '../../gestion-estacionamientos/shared/api-error';

/** Estado común de los dos CRUD; el interceptor existente envía el JWT. */
@Directive()
export abstract class CrudPage<T extends { id: string | number; nombre: string }> {
  private readonly auth = inject(AuthService);
  readonly esAdmin = computed(() => this.auth.session()?.rol === 'ADMIN');
  readonly registros = signal<T[]>([]);
  readonly cargando = signal(false);
  readonly guardando = signal(false);
  readonly cargaCorrecta = signal(false);
  readonly error = signal('');
  readonly exito = signal('');
  readonly editando = signal<T | null>(null);
  readonly porEliminar = signal<T | null>(null);
  abstract readonly form: FormGroup;
  protected abstract listar(): Observable<T[]>;
  protected abstract borrar(registro: T): Observable<void>;
  protected abstract reiniciarFormulario(): void;
  protected abstract llenarFormulario(registro: T): void;

  async cargar(): Promise<void> {
    if (this.cargando()) return;
    this.cargando.set(true); this.error.set(''); this.cargaCorrecta.set(false);
    try {
      this.registros.set(await firstValueFrom(this.listar()));
      this.cargaCorrecta.set(true);
    } catch (error) { this.error.set(mensajeError(error)); }
    finally { this.cargando.set(false); }
  }
  editar(registro: T): void {
    if (!this.esAdmin() || this.guardando() || this.cargando()) return;
    this.editando.set(registro); this.porEliminar.set(null);
    this.error.set(''); this.exito.set(''); this.llenarFormulario(registro);
  }
  cancelar(): void {
    if (this.guardando()) return;
    this.limpiarFormulario(); this.error.set('');
  }
  private limpiarFormulario(): void {
    this.editando.set(null); this.porEliminar.set(null); this.reiniciarFormulario();
  }
  confirmarEliminacion(registro: T): void {
    if (!this.esAdmin() || this.guardando() || this.cargando()) return;
    this.porEliminar.set(registro); this.error.set(''); this.exito.set('');
  }
  protected async persistir(solicitud: () => Observable<T>): Promise<void> {
    if (!this.esAdmin() || this.guardando() || this.cargando() || !this.cargaCorrecta()) return;
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    const actualizando = this.editando() !== null;
    this.guardando.set(true); this.error.set(''); this.exito.set('');
    try {
      const registro = await firstValueFrom(solicitud());
      this.limpiarFormulario();
      this.exito.set(`«${registro.nombre}» ${actualizando ? 'actualizado' : 'creado'} correctamente.`);
      await this.cargar();
    } catch (error) { this.error.set(mensajeError(error)); }
    finally { this.guardando.set(false); }
  }
  async eliminar(): Promise<void> {
    const registro = this.porEliminar();
    if (!registro || !this.esAdmin() || this.guardando() || this.cargando()) return;
    this.guardando.set(true); this.error.set(''); this.exito.set('');
    try {
      await firstValueFrom(this.borrar(registro));
      this.limpiarFormulario(); this.exito.set(`«${registro.nombre}» eliminado correctamente.`);
      await this.cargar();
    } catch (error) { this.error.set(mensajeError(error)); }
    finally { this.guardando.set(false); }
  }
}
