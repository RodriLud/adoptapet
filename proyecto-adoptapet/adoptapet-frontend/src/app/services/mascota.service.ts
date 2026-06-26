import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Mascota } from '../models/mascota';

@Injectable({
  providedIn: 'root',
})
export class MascotaService {
  private apiUrl = `${environment.apiUrl}/mascota`;

  constructor(private http: HttpClient) {}

  listarMascotas(): Observable<Mascota[]> {
    return this.http.get<Mascota[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<Mascota> {
    return this.http.get<Mascota>(`${this.apiUrl}/${id}`);
  }

  guardarMascota(mascota: Mascota, archivo?: File | null): Observable<Mascota> {
    const formData = this.toFormData(mascota, archivo);
    if (mascota.id_mascota) {
      return this.http.put<Mascota>(`${this.apiUrl}/actualizar/${mascota.id_mascota}`, formData);
    }
    return this.http.post<Mascota>(`${this.apiUrl}/registrar`, formData);
  }

  eliminarMascota(id: number, version?: number): Observable<any> {
    const url = version === undefined ? `${this.apiUrl}/eliminar/${id}` : `${this.apiUrl}/eliminar/${id}?version=${version}`;
    return this.http.delete(url);
  }

  private toFormData(mascota: Mascota, archivo?: File | null): FormData {
    const formData = new FormData();
    formData.append('nombre', mascota.nombre || '');
    formData.append('especie', mascota.especie || '');
    formData.append('raza', mascota.raza || '');
    formData.append('sexo', mascota.sexo || 'MACHO');
    formData.append('edad_anios', String(mascota.edad_anios ?? 0));
    formData.append('edad_meses', String(mascota.edad_meses ?? 0));
    formData.append('est_salud', mascota.est_salud || 'SIN NOVEDADES');
    formData.append('est_adopcion', mascota.est_adopcion || 'DISPONIBLE');
    formData.append('estado_esterilizacion', mascota.estado_esterilizacion || 'PENDIENTE');
    formData.append('observaciones', mascota.observaciones || '');
    if (mascota.version !== undefined && mascota.version !== null) {
      formData.append('version', String(mascota.version));
    }
    if (archivo) {
      formData.append('archivo', archivo);
    }
    return formData;
  }
}
