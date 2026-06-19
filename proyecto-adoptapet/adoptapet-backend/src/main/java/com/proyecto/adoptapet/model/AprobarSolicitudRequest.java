package com.proyecto.adoptapet.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.Data;

@Data
public class AprobarSolicitudRequest {
	private LocalDate fecha_entrega;
	private LocalTime hora_inicio;
	private LocalTime hora_fin;
	private LocalDateTime fecha_limite_recojo;
	private String observacion;
}
