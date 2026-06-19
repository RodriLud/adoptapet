package com.proyecto.adoptapet.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.proyecto.adoptapet.model.Mascota;
import com.proyecto.adoptapet.repository.MascotaRepository;
import com.proyecto.adoptapet.repository.SolicitudRepository;

@Service
public class MascotaService {

	@Autowired
	private MascotaRepository repo;
	@Autowired
	private SolicitudRepository solicitudRepo;

	private final String folder = "./fotos_mascotas/";

	public List<Mascota> listarTodas() {
		return repo.findAll();
	}

	public Mascota buscarPorId(Integer id) {
		return repo.findById(id).orElse(null);
	}

	public Mascota guardar(Mascota mascota) {
		validarDuplicado(mascota);
		validarEstadoAdopcion(mascota);
		return repo.save(mascota);
	}

	public Mascota guardarConImagen(Mascota mascota, MultipartFile archivo) throws IOException {
		if (archivo != null && !archivo.isEmpty()) {
			Path directorioImagenes = Paths.get(folder);
			if (!Files.exists(directorioImagenes)) {
				Files.createDirectories(directorioImagenes);
			}
			String nombreUnico = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
			Path rutaCompleta = directorioImagenes.resolve(nombreUnico);
			Files.copy(archivo.getInputStream(), rutaCompleta);
			mascota.setRuta_imagen(nombreUnico);
		} else if (mascota.getId_mascota() != null) {
			Mascota existente = repo.findById(mascota.getId_mascota()).orElse(null);
			if (existente != null) {
				mascota.setRuta_imagen(existente.getRuta_imagen());
			}
		}
		validarDuplicado(mascota);
		validarEstadoAdopcion(mascota);
		return repo.save(mascota);
	}

	private void validarDuplicado(Mascota mascota) {
		if (mascota.getNombre() == null || mascota.getEspecie() == null || mascota.getRaza() == null) {
			return;
		}
		if (repo.existeMascotaActivaSimilar(mascota.getNombre(), mascota.getEspecie(), mascota.getRaza(),
				mascota.getId_mascota())) {
			throw new RuntimeException("Ya existe una mascota activa con el mismo nombre, especie y raza");
		}
	}

	private void validarEstadoAdopcion(Mascota mascota) {
		if (mascota.getId_mascota() == null) {
			if ("ADOPTADO".equals(mascota.getEst_adopcion()) || "RESERVADO".equals(mascota.getEst_adopcion())) {
				throw new RuntimeException("Una mascota nueva debe registrarse como disponible");
			}
			return;
		}

		boolean tieneSolicitudFinalizada = solicitudRepo.existeSolicitudPorMascotaExcluyendo(mascota.getId_mascota(),
				"FINALIZADA", 0);
		boolean tieneProcesoActivo = solicitudRepo.existeSolicitudPorMascotaYEstadosExcluyendo(mascota.getId_mascota(),
				List.of("PENDIENTE", "APROBADA", "NO_ASISTIO"), 0);
		if (tieneSolicitudFinalizada && !"ADOPTADO".equals(mascota.getEst_adopcion())) {
			throw new RuntimeException("No se puede cambiar una mascota con adopcion finalizada");
		}
		if (tieneProcesoActivo && !"RESERVADO".equals(mascota.getEst_adopcion())) {
			throw new RuntimeException("No se puede cambiar una mascota con proceso de adopcion activo");
		}
		if (!tieneSolicitudFinalizada && "ADOPTADO".equals(mascota.getEst_adopcion())) {
			throw new RuntimeException("Para marcar una mascota como adoptada debe finalizarse una solicitud");
		}
	}
}
