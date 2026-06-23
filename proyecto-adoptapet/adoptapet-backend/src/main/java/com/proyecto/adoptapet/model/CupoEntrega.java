package com.proyecto.adoptapet.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uq_cupo_entrega_horario", columnNames = {
		"fecha_entrega", "hora_inicio", "hora_fin"
}))
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CupoEntrega {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id_cupo;

	private LocalDate fecha_entrega;

	private LocalTime hora_inicio;

	private LocalTime hora_fin;

	@Column(nullable = false)
	private Integer capacidad = 2;

	@Column(nullable = false)
	private Integer reservadas = 0;
}
