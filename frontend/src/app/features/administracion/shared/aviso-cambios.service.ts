import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AvisoCambiosService {
  readonly abierto = signal(false);
  private pendiente?: Promise<boolean>;
  private resolver?: (salir: boolean) => void;

  confirmar(): Promise<boolean> {
    if (this.pendiente) return this.pendiente;
    this.pendiente = new Promise(resolve => { this.resolver = resolve; });
    this.abierto.set(true);
    return this.pendiente;
  }
  responder(salir: boolean): void {
    this.abierto.set(false);
    this.resolver?.(salir);
    this.resolver = undefined;
    this.pendiente = undefined;
  }
}