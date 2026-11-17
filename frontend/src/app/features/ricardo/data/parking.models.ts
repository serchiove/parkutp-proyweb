export type TipoMovimiento = 'ENTRADA' | 'SALIDA';
export interface Estacionamiento {
  id: number;
  sedeId: string;
  sedeNombre: string;
  nombre: string;
  capacidad: number;
  activo: boolean;
  ocupados: number;
}
export interface Acceso {
  id: number;
  estacionamientoId: number;
  nombre: string;
  tipo: TipoMovimiento;
}
export interface AccesoRequest { nombre: string; tipo: TipoMovimiento; }
export interface MovimientoRequest {
  eventoId: string;
  tipo: TipoMovimiento;
  origen: 'MANUAL';
  placa: string | null;
}
export interface Movimiento extends Omit<MovimientoRequest, 'origen'> {
  id: number;
  estacionamientoId: number;
  accesoId: number;
  origen: 'MANUAL' | 'SIMULADOR';
  registradoEn: string;
}
export interface FiltrosHistorial { fecha: string; placa: string; tipo: TipoMovimiento | ''; }
