import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ReporteService {
  private apiUrl = `${environment.apiUrl}/reporte`;

  constructor(private http: HttpClient) {}

  descargarReporteGeneral(filtros?: {
    estadoSolicitud?: string;
    estadoMascota?: string;
    especie?: string;
  }): Observable<Blob> {
    let params = new HttpParams();
    if (filtros?.estadoSolicitud) {
      params = params.set('estadoSolicitud', filtros.estadoSolicitud);
    }
    if (filtros?.estadoMascota) {
      params = params.set('estadoMascota', filtros.estadoMascota);
    }
    if (filtros?.especie) {
      params = params.set('especie', filtros.especie);
    }

    return this.http.get(`${this.apiUrl}/general/pdf`, {
      params,
      responseType: 'blob',
    });
  }
}
