package com.proyecto.adoptapet.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

import com.proyecto.adoptapet.model.ApiResponse;
import com.proyecto.adoptapet.model.AprobarSolicitudRequest;
import com.proyecto.adoptapet.model.MotivoSolicitudRequest;
import com.proyecto.adoptapet.model.Solicitud;
import com.proyecto.adoptapet.service.SolicitudService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/solicitud")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class SolicitudController {

	private final SolicitudService service;

	@GetMapping
	public ResponseEntity<?> listar(@RequestParam(value = "estado", required = false) String estado) {
		return ResponseEntity.ok(service.listarSolicitudes(estado));
	}

	@GetMapping("/adoptante/{idAdoptante}")
	public ResponseEntity<?> listarPorAdoptante(@PathVariable Integer idAdoptante) {
		return ResponseEntity.ok(service.listarPorAdoptante(idAdoptante));
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> buscarPorId(@PathVariable(name = "id") int idSolicitud) {
		Solicitud solicitud = service.buscarPorId(idSolicitud);
		if (solicitud == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(solicitud);
	}

	@GetMapping("/disponibilidad")
	public ResponseEntity<?> disponibilidad(@RequestParam("fecha") LocalDate fecha) {
		try {
			return ResponseEntity.ok(service.listarDisponibilidadHorarios(fecha));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
		}
	}

	@PostMapping(value = "/registrar", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> registrarJson(@RequestBody Solicitud solicitud) {
		try {
			return ResponseEntity.ok(service.guardar(solicitud));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
		}
	}

	@PostMapping(value = "/registrar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> registrarFormulario(
	        @RequestParam("id_adoptante") Integer idAdoptante,
	        @RequestParam("id_mascota") Integer idMascota,
	        @RequestParam("comentario") String comentario,
	        @RequestParam("motivo_adopcion") String motivoAdopcion,
	        @RequestParam("tipo_vivienda") String tipoVivienda,
	        @RequestParam("experiencia_mascotas") String experienciaMascotas,
	        @RequestParam("otras_mascotas") String otrasMascotas,
	        @RequestParam("cantidad_personas_hogar") Integer cantidadPersonasHogar,
	        @RequestParam("comentarios_adicionales") String comentariosAdicionales,
	        @RequestParam(value = "archivo_dni", required = false) MultipartFile archivoDni,
	        @RequestParam(value = "archivo_domicilio", required = false) MultipartFile archivoDomicilio) {
		try {
			Solicitud nueva = service.guardarManual(idAdoptante, idMascota, comentario, motivoAdopcion, tipoVivienda,
					experienciaMascotas, otrasMascotas, cantidadPersonasHogar, comentariosAdicionales, archivoDni,
					archivoDomicilio);
			return ResponseEntity.ok(nueva);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
		}
	}

	@PutMapping("/aprobar/{id}")
	public ResponseEntity<?> aprobar(@PathVariable Integer id,
			@RequestBody(required = false) AprobarSolicitudRequest request, Authentication authentication) {
		try {
			String username = authentication == null ? null : authentication.getName();
			return ResponseEntity.ok(service.aprobarSolicitud(id, username, request));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
		}
	}

	@PutMapping("/rechazar/{id}")
	public ResponseEntity<?> rechazar(@PathVariable Integer id,
			@RequestBody(required = false) MotivoSolicitudRequest request) {
		try {
			String motivo = request == null ? null : request.getMotivo();
			return ResponseEntity.ok(service.rechazarSolicitud(id, motivo));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
		}
	}

	@PutMapping("/finalizar/{id}")
	public ResponseEntity<?> finalizar(@PathVariable Integer id, Authentication authentication) {
		try {
			String username = authentication == null ? null : authentication.getName();
			return ResponseEntity.ok(service.finalizarSolicitud(id, username));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
		}
	}

	@PutMapping("/finalizar-contingencia/{id}")
	public ResponseEntity<?> finalizarPorContingencia(@PathVariable Integer id,
			@RequestBody(required = false) MotivoSolicitudRequest request, Authentication authentication) {
		try {
			String username = authentication == null ? null : authentication.getName();
			String motivo = request == null ? null : request.getMotivo();
			return ResponseEntity.ok(service.finalizarPorContingencia(id, username, motivo));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
		}
	}

	@PutMapping("/cancelar/{id}")
	public ResponseEntity<?> cancelar(@PathVariable Integer id,
			@RequestBody(required = false) MotivoSolicitudRequest request) {
		try {
			String motivo = request == null ? null : request.getMotivo();
			return ResponseEntity.ok(service.cancelarSolicitud(id, motivo));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
		}
	}

	@PutMapping("/no-asistio/{id}")
	public ResponseEntity<?> marcarNoAsistio(@PathVariable Integer id) {
		try {
			return ResponseEntity.ok(service.marcarNoAsistio(id));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
		}
	}

	@PutMapping("/reprogramar/{id}")
	public ResponseEntity<?> reprogramar(@PathVariable Integer id, @RequestBody AprobarSolicitudRequest request,
			Authentication authentication) {
		try {
			String username = authentication == null ? null : authentication.getName();
			return ResponseEntity.ok(service.reprogramarEntrega(id, request, username));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
		}
	}

	@GetMapping("/documento/{id}")
	public ResponseEntity<?> generarDocumento(@PathVariable Integer id, Authentication authentication) {
		try {
			String username = authentication == null ? null : authentication.getName();
			Resource documento = service.generarDocumento(id, username);
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
					.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + documento.getFilename() + "\"")
					.body(documento);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
		}
	}

	@PostMapping(value = "/acta-firmada/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> subirActaFirmada(@PathVariable Integer id,
			@RequestParam("archivo_acta_firmada") MultipartFile archivoActaFirmada) {
		try {
			return ResponseEntity.ok(service.subirActaFirmada(id, archivoActaFirmada));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
		}
	}

}
