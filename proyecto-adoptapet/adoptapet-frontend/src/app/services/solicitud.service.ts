import { environment } from '../../environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Solicitud } from '../models/solicitud';

export interface AprobarSolicitudPayload {
  fecha_entrega: string;
  hora_inicio: string;
  hora_fin: string;
  fecha_limite_recojo: string;
  observacion?: string;
}

export interface HorarioDisponibilidad {
  label: string;
  hora_inicio: string;
  hora_fin: string;
  capacidad: number;
  reservadas: number;
  disponibles: number;
  disponible: boolean;
  vencido: boolean;
}

@Injectable({ providedIn: 'root' })
export class SolicitudService {
  private apiUrl = `${environment.apiUrl}/solicitud`;

  constructor(private http: HttpClient) {}

  listar(estado?: string): Observable<Solicitud[]> {
    const url = estado ? `${this.apiUrl}?estado=${estado}` : this.apiUrl;
    return this.http.get<Solicitud[]>(url);
  }

  buscarPorId(id: number): Observable<Solicitud> {
    return this.http.get<Solicitud>(`${this.apiUrl}/${id}`);
  }

  disponibilidad(fecha: string): Observable<HorarioDisponibilidad[]> {
    return this.http.get<HorarioDisponibilidad[]>(`${this.apiUrl}/disponibilidad?fecha=${fecha}`);
  }

  aprobar(id: number, payload: AprobarSolicitudPayload): Observable<Solicitud> {
    return this.http.put<Solicitud>(`${this.apiUrl}/aprobar/${id}`, payload);
  }

  rechazar(id: number, motivo: string): Observable<Solicitud> {
    return this.http.put<Solicitud>(`${this.apiUrl}/rechazar/${id}`, { motivo });
  }

  finalizar(id: number): Observable<Solicitud> {
    return this.http.put<Solicitud>(`${this.apiUrl}/finalizar/${id}`, {});
  }

  finalizarPorContingencia(id: number, motivo: string): Observable<Solicitud> {
    return this.http.put<Solicitud>(`${this.apiUrl}/finalizar-contingencia/${id}`, { motivo });
  }

  marcarNoAsistio(id: number): Observable<Solicitud> {
    return this.http.put<Solicitud>(`${this.apiUrl}/no-asistio/${id}`, {});
  }

  reprogramar(id: number, payload: AprobarSolicitudPayload): Observable<Solicitud> {
    return this.http.put<Solicitud>(`${this.apiUrl}/reprogramar/${id}`, payload);
  }

  cancelar(id: number, motivo: string): Observable<Solicitud> {
    return this.http.put<Solicitud>(`${this.apiUrl}/cancelar/${id}`, { motivo });
  }

  generarDocumento(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/documento/${id}`, {
      responseType: 'blob',
    });
  }

  subirActaFirmada(id: number, archivo: File): Observable<Solicitud> {
    const formData = new FormData();
    formData.append('archivo_acta_firmada', archivo);
    return this.http.post<Solicitud>(`${this.apiUrl}/acta-firmada/${id}`, formData);
  }
}
