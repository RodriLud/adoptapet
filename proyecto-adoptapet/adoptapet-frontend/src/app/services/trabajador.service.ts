import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TrabajadorService {
  private apiUrl = 'http://localhost:8080/trabajador';

  constructor(private http: HttpClient) {}

  listar(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  registrar(trabajador: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/registrar`, trabajador);
  }

  actualizar(id: number, trabajador: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/actualizar/${id}`, trabajador);
  }

  eliminar(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/eliminar/${id}`);
  }

  activar(id: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/activar/${id}`, {});
  }
}
