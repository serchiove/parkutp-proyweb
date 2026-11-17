import { Component, inject, input, OnInit, output } from '@angular/core';
import { ActivatedRoute, RouterLink, RouterLinkActive } from '@angular/router';
import { ParkingContext } from './parking-context';

@Component({
  selector: 'app-parking-selector',
  imports: [RouterLink, RouterLinkActive],
  template: `
    <nav class="module-nav" aria-label="Módulos de Ricardo">
      <a routerLink="/accesos" routerLinkActive="selected" [queryParams]="{ estacionamiento: ctx.seleccionado()?.id }">Accesos</a>
      <a routerLink="/operacion" routerLinkActive="selected" [queryParams]="{ estacionamiento: ctx.seleccionado()?.id }">Operación</a>
      <a routerLink="/historial" routerLinkActive="selected" [queryParams]="{ estacionamiento: ctx.seleccionado()?.id }">Historial</a>
    </nav>
    <section class="card selector" aria-label="Seleccionar estacionamiento">
      <label for="parking">Estacionamiento
        <select id="parking" [value]="ctx.seleccionado()?.id ?? ''" [disabled]="disabled() || ctx.cargando()" (change)="seleccionar($event)">
          @if (!ctx.seleccionado()) { <option value="">Selecciona un estacionamiento</option> }
          @for (e of ctx.estacionamientos(); track e.id) {
            <option [value]="e.id">{{ e.sedeNombre }} — {{ e.nombre }}{{ e.activo ? '' : ' (inactivo)' }}</option>
          }
        </select>
      </label>
      <button type="button" [disabled]="disabled() || ctx.cargando()" (click)="cargar()">Actualizar estacionamientos</button>
    </section>
    @if (ctx.cargando()) { <p role="status">Cargando estacionamientos…</p> }
    @if (ctx.error()) { <p class="notice error" role="alert">{{ ctx.error() }}</p> }
    @if (!ctx.cargando() && !ctx.error() && !ctx.estacionamientos().length) {
      <p class="notice">No hay estacionamientos. Solicita a administración que registre uno.</p>
    }
  `,
  styleUrl: './ricardo.scss',
})
export class ParkingSelector implements OnInit {
  readonly ctx = inject(ParkingContext);
  private readonly route = inject(ActivatedRoute);
  readonly disabled = input(false);
  readonly preferido = input<number | undefined>(undefined);
  readonly changed = output<void>();
  ngOnInit(): void { void this.cargar(); }
  async cargar(): Promise<void> {
    const preferido = this.preferido() ?? this.ctx.seleccionado()?.id ?? Number(this.route.snapshot.queryParamMap.get('estacionamiento'));
    await this.ctx.cargar(preferido);
    this.changed.emit();
  }
  seleccionar(event: Event): void {
    this.ctx.seleccionar(Number((event.target as HTMLSelectElement).value));
    this.changed.emit();
  }
}
