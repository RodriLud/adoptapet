import { Mascota } from './mascota';

export interface ProgramacionEntrega {
  id_programacion?: number;
  fecha_entrega?: string;
  hora_inicio?: string;
  hora_fin?: string;
  fecha_limite_recojo?: string;
  estado_programacion?: string;
  motivo_cancelacion?: string;
  observacion?: string;
}

export interface Solicitud {
  id_solicitud?: number;
  fecha_registro?: string;
  adoptante?: any;
  mascota?: Mascota;
  comentario?: string;
  acta_pdf?: string;
  motivo_adopcion?: string;
  tipo_vivienda?: string;
  experiencia_mascotas?: string;
  otras_mascotas?: string;
  cantidad_personas_hogar?: number;
  comentarios_adicionales?: string;
  ruta_dni?: string;
  ruta_domicilio?: string;
  ruta_acta_firmada?: string;
  cantidad_reprogramaciones?: number;
  motivo_contingencia?: string;
  fecha_cierre_contingencia?: string;
  estado_solicitud?: string;
  motivo_rechazo?: string;
  programacionEntrega?: ProgramacionEntrega;
}
