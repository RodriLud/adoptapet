package com.proyecto.adoptapet.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HorarioDisponibilidad {
	private String label;
	private String hora_inicio;
	private String hora_fin;
	private Integer capacidad;
	private Integer reservadas;
	private Integer disponibles;
	private Boolean disponible;
	private Boolean vencido;
}
