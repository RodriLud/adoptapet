package com.proyecto.adoptapet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyecto.adoptapet.model.Solicitud;

public interface SolicitudRepository extends JpaRepository<Solicitud, Integer>{
	@Query("SELECT s FROM Solicitud s JOIN FETCH s.adoptante JOIN FETCH s.mascota LEFT JOIN FETCH s.programacionEntrega ORDER BY s.id_solicitud")
	List<Solicitud> listarSolicitudes();

	@Query("SELECT s FROM Solicitud s JOIN FETCH s.adoptante JOIN FETCH s.mascota LEFT JOIN FETCH s.programacionEntrega WHERE s.estado_solicitud = :estado ORDER BY s.id_solicitud")
	List<Solicitud> listarSolicitudesPorEstado(@Param("estado") String estado);

	@Query("SELECT s FROM Solicitud s JOIN FETCH s.adoptante JOIN FETCH s.mascota LEFT JOIN FETCH s.programacionEntrega WHERE s.id_solicitud = :idSolicitud")
	Optional<Solicitud> buscarDetallePorId(@Param("idSolicitud") Integer idSolicitud);

	@Query("SELECT COUNT(s) > 0 FROM Solicitud s WHERE s.adoptante.id_usuario = :idAdoptante AND s.mascota.id_mascota = :idMascota AND s.estado_solicitud = :estado")
	boolean existeSolicitudActiva(@Param("idAdoptante") Integer idAdoptante, @Param("idMascota") Integer idMascota,
			@Param("estado") String estado);

	@Query("SELECT COUNT(s) > 0 FROM Solicitud s WHERE s.mascota.id_mascota = :idMascota AND s.estado_solicitud = :estado AND s.id_solicitud <> :idSolicitud")
	boolean existeSolicitudPorMascotaExcluyendo(@Param("idMascota") Integer idMascota, @Param("estado") String estado,
			@Param("idSolicitud") Integer idSolicitud);

	@Query("SELECT COUNT(s) > 0 FROM Solicitud s WHERE s.mascota.id_mascota = :idMascota AND s.estado_solicitud IN (:estados) AND s.id_solicitud <> :idSolicitud")
	boolean existeSolicitudPorMascotaYEstadosExcluyendo(@Param("idMascota") Integer idMascota,
			@Param("estados") List<String> estados, @Param("idSolicitud") Integer idSolicitud);

	@Query("SELECT s FROM Solicitud s WHERE s.mascota.id_mascota = :idMascota AND s.estado_solicitud = :estado AND s.id_solicitud <> :idSolicitud")
	List<Solicitud> listarPorMascotaYEstadoExcluyendo(@Param("idMascota") Integer idMascota,
			@Param("estado") String estado, @Param("idSolicitud") Integer idSolicitud);

	@Query("SELECT s FROM Solicitud s JOIN FETCH s.mascota WHERE s.adoptante.id_usuario = :idAdoptante ORDER BY s.fecha_registro DESC, s.id_solicitud DESC")
	List<Solicitud> listarHistorialPorAdoptante(@Param("idAdoptante") Integer idAdoptante);
}
