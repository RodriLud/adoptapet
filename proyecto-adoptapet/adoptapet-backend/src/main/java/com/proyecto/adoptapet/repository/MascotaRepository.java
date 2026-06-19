package com.proyecto.adoptapet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyecto.adoptapet.model.Mascota;

import jakarta.persistence.LockModeType;

public interface MascotaRepository extends JpaRepository<Mascota, Integer> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT m FROM Mascota m WHERE m.id_mascota = :id")
	Optional<Mascota> findByIdForUpdate(@Param("id") Integer id);

	@Query("""
			SELECT COUNT(m) > 0
			FROM Mascota m
			WHERE LOWER(TRIM(m.nombre)) = LOWER(TRIM(:nombre))
			  AND LOWER(TRIM(m.especie)) = LOWER(TRIM(:especie))
			  AND LOWER(TRIM(m.raza)) = LOWER(TRIM(:raza))
			  AND m.est_adopcion <> 'INACTIVO'
			  AND (:idMascota IS NULL OR m.id_mascota <> :idMascota)
			""")
	boolean existeMascotaActivaSimilar(@Param("nombre") String nombre, @Param("especie") String especie,
			@Param("raza") String raza, @Param("idMascota") Integer idMascota);
}
