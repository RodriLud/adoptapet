package com.proyecto.adoptapet.model;

import java.sql.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdoptanteResumen {
	private Integer id_adoptante;
	private String nom_adoptante;
	private String ape_adoptante;
	private String dni;
	private Date fec_nacimiento;
	private String email;
	private String telefono;
	private String direccion;
	private Boolean activo;
	private Integer total_solicitudes;
	private Integer pendientes;
	private Integer aprobadas;
	private Integer no_asistio;
	private Integer finalizadas;
	private Integer rechazadas;
	private Integer canceladas;
	private Date ultima_solicitud;
	private List<SolicitudHistorialResumen> solicitudes;
}
