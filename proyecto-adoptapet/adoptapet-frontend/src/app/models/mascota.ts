export interface Mascota {
  id_mascota?: number;
  nombre: string;
  especie: string;
  raza: string;
  sexo?: string;
  edad_anios: number;
  edad_meses: number;
  est_salud: string;
  est_adopcion: string;
  estado_esterilizacion?: string;
  observaciones?: string;
  ruta_imagen?: string;
  version?: number;
}
