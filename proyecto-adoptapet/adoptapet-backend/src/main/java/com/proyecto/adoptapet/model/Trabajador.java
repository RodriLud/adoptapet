package com.proyecto.adoptapet.model;

import java.sql.Date;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true) // Para que Lombok no ignore los campos de Usuario
public class Trabajador extends Usuario{
	private String nom_trabajador;
	
	private String ape_trabajador;
	
	private String dni;
	
	private Date fec_nacimiento;
	
	private String email;
	
	private String telefono;
}
