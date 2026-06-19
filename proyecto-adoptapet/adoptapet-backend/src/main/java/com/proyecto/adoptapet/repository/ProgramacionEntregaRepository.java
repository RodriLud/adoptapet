package com.proyecto.adoptapet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyecto.adoptapet.model.ProgramacionEntrega;

public interface ProgramacionEntregaRepository extends JpaRepository<ProgramacionEntrega, Integer> {
	@Query("SELECT p FROM ProgramacionEntrega p WHERE p.solicitud.id_solicitud = :idSolicitud")
	Optional<ProgramacionEntrega> buscarPorSolicitud(@Param("idSolicitud") Integer idSolicitud);
}
