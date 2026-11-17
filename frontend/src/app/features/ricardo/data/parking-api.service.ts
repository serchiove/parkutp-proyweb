import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_BASE_URL } from '../../../core/api/api-config';
import { Acceso, AccesoRequest, Estacionamiento, FiltrosHistorial, Movimiento, MovimientoRequest } from './parking.models';

@Injectable({ providedIn: 'root' })
export class ParkingApiService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE_URL}/estacionamientos`;

  listarEstacionamientos() { return this.http.get<Estacionamiento[]>(this.url); }
  buscarEstacionamiento(id: number) { return this.http.get<Estacionamiento>(`${this.url}/${id}`); }
  listarAccesos(id: number) { return this.http.get<Acceso[]>(`${this.url}/${id}/accesos`); }
  crearAcceso(id: number, data: AccesoRequest) {
    return this.http.post<Acceso>(`${this.url}/${id}/accesos`, data);
  }
  actualizarAcceso(id: number, accesoId: number, data: AccesoRequest) {
    return this.http.put<Acceso>(`${this.url}/${id}/accesos/${accesoId}`, data);
  }
  eliminarAcceso(id: number, accesoId: number) {
    return this.http.delete<void>(`${this.url}/${id}/accesos/${accesoId}`);
  }
  registrarMovimiento(id: number, data: MovimientoRequest) {
    return this.http.post<Movimiento>(`${this.url}/${id}/movimientos`, data);
  }
  listarMovimientos(id: number, filtros?: FiltrosHistorial) {
    let params = new HttpParams();
    if (filtros?.fecha) params = params.set('fecha', filtros.fecha);
    if (filtros?.placa.trim()) params = params.set('placa', filtros.placa.trim().toUpperCase());
    if (filtros?.tipo) params = params.set('tipo', filtros.tipo);
    return this.http.get<Movimiento[]>(`${this.url}/${id}/movimientos`, { params });
  }
}
