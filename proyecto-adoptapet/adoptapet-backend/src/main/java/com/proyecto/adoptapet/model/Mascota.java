package com.proyecto.adoptapet.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Mascota {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id_mascota;
	
	@Column(length = 50)
	private String nombre;
	
	@Column(length = 20)
	private String especie;
	
	@Column(length = 50)
	private String raza;

	@Column(length = 20)
	private String sexo;
	
	private Integer edad_anios;
	
	private Integer edad_meses;
	
	@Column(name = "estado_salud", length = 50)
    private String est_salud;
    
	@Column(name = "estado_adopcion", length = 20)
    private String est_adopcion;

	@Column(name = "estado_esterilizacion", length = 30)
	private String estado_esterilizacion;

	@Column(length = 500)
	private String observaciones;
    
	@Column(length = 255)
	private String ruta_imagen;

	@Version
	private Long version;
}
