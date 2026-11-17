import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_BASE_URL } from '../../../core/api/api-config';

export interface Sede { id: string; nombre: string; }

@Injectable({ providedIn: 'root' })
export class AdministracionApiService {
  private readonly http = inject(HttpClient);
  listarSedes() { return this.http.get<Sede[]>(`${API_BASE_URL}/sedes`); }
  crearSede(data: Sede) { return this.http.post<Sede>(`${API_BASE_URL}/sedes`, data); }
  actualizarSede(id: string, data: Sede) {
    return this.http.put<Sede>(`${API_BASE_URL}/sedes/${encodeURIComponent(id)}`, data);
  }
  eliminarSede(id: string) { return this.http.delete<void>(`${API_BASE_URL}/sedes/${encodeURIComponent(id)}`); }
}
