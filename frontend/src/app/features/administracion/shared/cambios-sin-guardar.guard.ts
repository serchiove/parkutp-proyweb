import { CanDeactivateFn } from '@angular/router';

export interface FormularioConCambios {
  puedeSalir(): boolean | Promise<boolean>;
}

export const cambiosSinGuardarGuard: CanDeactivateFn<FormularioConCambios> = componente => componente.puedeSalir();