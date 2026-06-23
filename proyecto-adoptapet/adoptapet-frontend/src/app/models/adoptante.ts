export interface SolicitudHistorialResumen {
  id_solicitud?: number;
  fecha_registro?: string;
  estado_solicitud?: string;
  mascota?: string;
  especie?: string;
}

export interface AdoptanteResumen {
  id_adoptante?: number;
  nom_adoptante?: string;
  ape_adoptante?: string;
  dni?: string;
  fec_nacimiento?: string;
  email?: string;
  telefono?: string;
  direccion?: string;
  activo?: boolean;
  total_solicitudes?: number;
  pendientes?: number;
  aprobadas?: number;
  no_asistio?: number;
  finalizadas?: number;
  rechazadas?: number;
  canceladas?: number;
  ultima_solicitud?: string;
  solicitudes?: SolicitudHistorialResumen[];
}
