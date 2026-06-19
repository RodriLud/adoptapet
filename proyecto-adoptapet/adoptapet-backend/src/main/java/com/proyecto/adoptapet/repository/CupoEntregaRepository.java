package com.proyecto.adoptapet.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyecto.adoptapet.model.CupoEntrega;

import jakarta.persistence.LockModeType;

public interface CupoEntregaRepository extends JpaRepository<CupoEntrega, Integer> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT c
			FROM CupoEntrega c
			WHERE c.fecha_entrega = :fecha
			  AND c.hora_inicio = :horaInicio
			  AND c.hora_fin = :horaFin
			""")
	Optional<CupoEntrega> buscarCupoParaActualizar(@Param("fecha") LocalDate fecha,
			@Param("horaInicio") LocalTime horaInicio, @Param("horaFin") LocalTime horaFin);

	@Query("SELECT c FROM CupoEntrega c WHERE c.fecha_entrega = :fecha")
	List<CupoEntrega> listarPorFecha(@Param("fecha") LocalDate fecha);
}
