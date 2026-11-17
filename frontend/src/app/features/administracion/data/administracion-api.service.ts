import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_BASE_URL } from '../../../core/api/api-config';
import { Estacionamiento } from '../../gestion-estacionamientos/data/parking.models';

export interface Sede { id: string; nombre: string; }
export interface EstacionamientoRequest { sedeId: string; nombre: string; capacidad: number; activo: boolean; }

@Injectable({ providedIn: 'root' })
export class AdministracionApiService {
  private readonly http = inject(HttpClient);
  listarSedes() { return this.http.get<Sede[]>(`${API_BASE_URL}/sedes`); }
  crearSede(data: Sede) { return this.http.post<Sede>(`${API_BASE_URL}/sedes`, data); }
  actualizarSede(id: string, data: Sede) {
    return this.http.put<Sede>(`${API_BASE_URL}/sedes/${encodeURIComponent(id)}`, data);
  }
  eliminarSede(id: string) { return this.http.delete<void>(`${API_BASE_URL}/sedes/${encodeURIComponent(id)}`); }
  listarEstacionamientos() { return this.http.get<Estacionamiento[]>(`${API_BASE_URL}/estacionamientos`); }
  crearEstacionamiento(data: EstacionamientoRequest) {
    return this.http.post<Estacionamiento>(`${API_BASE_URL}/estacionamientos`, data);
  }
  actualizarEstacionamiento(id: number, data: EstacionamientoRequest) {
    return this.http.put<Estacionamiento>(`${API_BASE_URL}/estacionamientos/${id}`, data);
  }
  eliminarEstacionamiento(id: number) { return this.http.delete<void>(`${API_BASE_URL}/estacionamientos/${id}`); }
}
