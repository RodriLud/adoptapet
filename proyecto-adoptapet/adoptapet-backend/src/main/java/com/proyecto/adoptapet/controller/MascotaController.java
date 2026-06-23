package com.proyecto.adoptapet.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.proyecto.adoptapet.model.ApiResponse;
import com.proyecto.adoptapet.model.Mascota;
import com.proyecto.adoptapet.service.MascotaService;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/mascota")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200") // Para que Angular pueda conectarse sin problemas de CORS
public class MascotaController {

	private final MascotaService service;

	@GetMapping
	public List<Mascota> listar() {
		return service.listarTodas();
	}

	// Buscar por ID
	@GetMapping("/{id}")
	public ResponseEntity<Mascota> buscarPorId(@PathVariable(name = "id") int id_mascota) {
		Mascota mascota = service.buscarPorId(id_mascota);
		if (mascota == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(mascota);
	}

	@PostMapping("/registrar")
	public ResponseEntity<?> registrar(@ModelAttribute Mascota mascota,
			@RequestParam(value = "archivo", required = false) MultipartFile archivo,
			@RequestParam(value = "foto", required = false) MultipartFile foto) {
		try {
			MultipartFile imagen = archivo != null ? archivo : foto;
			Mascota nuevaMascota = service.guardarConImagen(mascota, imagen);
			return ResponseEntity.ok(nuevaMascota);
		} catch (Exception e) {
			if (e instanceof DataIntegrityViolationException) {
				return ResponseEntity.status(HttpStatus.CONFLICT)
						.body(new ApiResponse("Ya existe una mascota activa con los mismos datos"));
			}
			return ResponseEntity.badRequest().body(new ApiResponse("Error al guardar mascota: " + e.getMessage()));
		}
	}

	// Actualizar la imagen también, se usa @ModelAttribute
	@PutMapping("/actualizar/{id}")
	public ResponseEntity<?> actualizar(@PathVariable(name = "id") int id_mascota, @ModelAttribute Mascota mascota,
			@RequestParam(value = "archivo", required = false) MultipartFile archivo,
			@RequestParam(value = "foto", required = false) MultipartFile foto) {
		try {
			MultipartFile imagen = archivo != null ? archivo : foto;
			Mascota temporal = service.buscarPorId(id_mascota);
			if (temporal == null) {
				return ResponseEntity.notFound().build();
			}
			// Aseguramos que el ID sea el correcto antes de guardar
			mascota.setId_mascota(id_mascota);
			Mascota actualizada = service.guardarConImagen(mascota, imagen);
			return ResponseEntity.ok(actualizada);
		} catch (OptimisticLockException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(
					"La mascota fue modificada por otro usuario. Actualiza la lista antes de guardar."));
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(new ApiResponse("Ya existe una mascota activa con los mismos datos"));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(new ApiResponse("Error al actualizar: " + e.getMessage()));
		}
	}

	@DeleteMapping("/eliminar/{id}")
	public ResponseEntity<?> eliminarLogico(@PathVariable Integer id,
			@RequestParam(value = "version", required = false) Long version) {
		try {
			Mascota mascota = service.buscarPorId(id);
			if (mascota == null)
				return ResponseEntity.notFound().build();
			if (version == null) {
				return ResponseEntity.badRequest()
						.body(new ApiResponse("La version de la mascota es obligatoria para dar de baja"));
			}
			if (!version.equals(mascota.getVersion())) {
				return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(
						"La mascota fue modificada por otro usuario. Actualiza la lista antes de dar de baja."));
			}

			// Cambio de estado a INACTIVO
			mascota.setEst_adopcion("INACTIVO");
			service.guardar(mascota);

			return ResponseEntity.ok(new ApiResponse("Mascota dada de baja correctamente"));
		} catch (OptimisticLockException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(
					"La mascota fue modificada por otro usuario. Actualiza la lista antes de dar de baja."));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(new ApiResponse("Error al procesar la baja: " + e.getMessage()));
		}
	}
}
