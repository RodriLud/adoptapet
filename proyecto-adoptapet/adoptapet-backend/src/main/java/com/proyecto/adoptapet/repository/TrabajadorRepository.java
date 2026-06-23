package com.proyecto.adoptapet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.proyecto.adoptapet.model.Trabajador;

public interface TrabajadorRepository extends JpaRepository<Trabajador, Integer>{

	Boolean existsByEmail(String email);

	Boolean existsByDni(String dni);

	Optional<Trabajador> findByUsername(String username);

	@Query("select t from Trabajador t where t.activo = true order by t.id_usuario asc limit 1")
	Optional<Trabajador> findPrimerActivo();
}
