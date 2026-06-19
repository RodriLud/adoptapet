package com.proyecto.adoptapet.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgramacionEntrega {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id_programacion;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_solicitud", nullable = false, unique = true)
	@JsonIgnore
	private Solicitud solicitud;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_cupo", nullable = false)
	@JsonIgnore
	private CupoEntrega cupo;

	private LocalDate fecha_entrega;

	private LocalTime hora_inicio;

	private LocalTime hora_fin;

	private LocalDateTime fecha_limite_recojo;

	@Column(length = 30)
	private String estado_programacion;

	private String motivo_cancelacion;

	private String observacion;
}
