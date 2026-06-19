package com.proyecto.adoptapet.model;

import java.sql.Date;

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
public class Solicitud {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id_solicitud;
	
	private Date fecha_registro;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="id_adoptante")
	private Adoptante adoptante;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="id_mascota")
	private Mascota mascota;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_trabajador")
	private Trabajador trabajador;
	
	private String comentario;
	
	@Column(name="ruta_pdf_acta")
	private String acta_pdf;

	private String motivo_adopcion;

	private String tipo_vivienda;

	private String experiencia_mascotas;

	private String otras_mascotas;

	private Integer cantidad_personas_hogar;

	private String comentarios_adicionales;

	private String ruta_dni;

	private String ruta_domicilio;

	private String ruta_acta_firmada;

	private Integer cantidad_reprogramaciones = 0;

	private String motivo_contingencia;

	private java.time.LocalDateTime fecha_cierre_contingencia;
	
	@Column(name = "estado_solicitud")
	private String estado_solicitud;

	private String motivo_rechazo;

	@OneToOne(mappedBy = "solicitud", fetch = FetchType.LAZY)
	private ProgramacionEntrega programacionEntrega;

}
