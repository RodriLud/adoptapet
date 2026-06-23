package com.proyecto.adoptapet.model;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudHistorialResumen {
	private Integer id_solicitud;
	private Date fecha_registro;
	private String estado_solicitud;
	private String mascota;
	private String especie;
}
