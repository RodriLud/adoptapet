package com.proyecto.adoptapet.service;

import java.sql.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.proyecto.adoptapet.model.Adoptante;
import com.proyecto.adoptapet.model.AdoptanteResumen;
import com.proyecto.adoptapet.model.Solicitud;
import com.proyecto.adoptapet.model.SolicitudHistorialResumen;
import com.proyecto.adoptapet.repository.AdoptanteRepository;
import com.proyecto.adoptapet.repository.SolicitudRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdoptanteService {
	private final AdoptanteRepository adoptanteRepo;
	private final SolicitudRepository solicitudRepo;

	public List<AdoptanteResumen> listarResumenes() {
		return adoptanteRepo.findAll().stream()
				.map((adoptante) -> construirResumen(adoptante, false))
				.toList();
	}

	public AdoptanteResumen buscarResumen(Integer idAdoptante) {
		Adoptante adoptante = adoptanteRepo.findById(idAdoptante)
				.orElseThrow(() -> new RuntimeException("Adoptante no encontrado"));
		return construirResumen(adoptante, true);
	}

	private AdoptanteResumen construirResumen(Adoptante adoptante, boolean incluirSolicitudes) {
		List<Solicitud> solicitudes = solicitudRepo.listarHistorialPorAdoptante(adoptante.getId_usuario());
		List<SolicitudHistorialResumen> historial = incluirSolicitudes
				? solicitudes.stream().map(this::mapearSolicitud).toList()
				: List.of();
		return new AdoptanteResumen(
				adoptante.getId_usuario(),
				adoptante.getNom_adoptante(),
				adoptante.getApe_adoptante(),
				adoptante.getDni(),
				adoptante.getFec_nacimiento(),
				adoptante.getEmail(),
				adoptante.getTelefono(),
				adoptante.getDireccion(),
				adoptante.getActivo(),
				solicitudes.size(),
				contar(solicitudes, "PENDIENTE"),
				contar(solicitudes, "APROBADA"),
				contar(solicitudes, "NO_ASISTIO"),
				contar(solicitudes, "FINALIZADA"),
				contar(solicitudes, "RECHAZADA"),
				contar(solicitudes, "CANCELADA"),
				ultimaSolicitud(solicitudes),
				historial);
	}

	private SolicitudHistorialResumen mapearSolicitud(Solicitud solicitud) {
		return new SolicitudHistorialResumen(
				solicitud.getId_solicitud(),
				solicitud.getFecha_registro(),
				solicitud.getEstado_solicitud(),
				solicitud.getMascota() == null ? "" : solicitud.getMascota().getNombre(),
				solicitud.getMascota() == null ? "" : solicitud.getMascota().getEspecie());
	}

	private Integer contar(List<Solicitud> solicitudes, String estado) {
		return (int) solicitudes.stream()
				.filter((solicitud) -> estado.equals(solicitud.getEstado_solicitud()))
				.count();
	}

	private Date ultimaSolicitud(List<Solicitud> solicitudes) {
		return solicitudes.stream()
				.map(Solicitud::getFecha_registro)
				.filter((fecha) -> fecha != null)
				.max(Date::compareTo)
				.orElse(null);
	}

	public Adoptante desactivar(Integer idAdoptante) {
		Adoptante adoptante = adoptanteRepo.findById(idAdoptante)
				.orElseThrow(() -> new RuntimeException("Adoptante no encontrado"));
		adoptante.setActivo(false);
		return adoptanteRepo.save(adoptante);
	}

	public Adoptante activar(Integer idAdoptante) {
		Adoptante adoptante = adoptanteRepo.findById(idAdoptante)
				.orElseThrow(() -> new RuntimeException("Adoptante no encontrado"));
		adoptante.setActivo(true);
		return adoptanteRepo.save(adoptante);
	}
}
