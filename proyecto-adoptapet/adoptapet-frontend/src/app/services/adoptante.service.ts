import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AdoptanteResumen } from '../models/adoptante';

@Injectable({
  providedIn: 'root',
})
export class AdoptanteService {
  private apiUrl = 'http://localhost:8080/adoptante';

  constructor(private http: HttpClient) {}

  listar(): Observable<AdoptanteResumen[]> {
    return this.http.get<AdoptanteResumen[]>(this.apiUrl);
  }

  buscarResumen(id: number): Observable<AdoptanteResumen> {
    return this.http.get<AdoptanteResumen>(`${this.apiUrl}/${id}`);
  }

  desactivar(id: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/desactivar/${id}`, {});
  }

  activar(id: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/activar/${id}`, {});
  }
}
