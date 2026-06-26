package com.proyecto.adoptapet.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.proyecto.adoptapet.model.Adoptante;
import com.proyecto.adoptapet.model.AprobarSolicitudRequest;
import com.proyecto.adoptapet.model.CupoEntrega;
import com.proyecto.adoptapet.model.HorarioDisponibilidad;
import com.proyecto.adoptapet.model.Mascota;
import com.proyecto.adoptapet.model.ProgramacionEntrega;
import com.proyecto.adoptapet.model.Solicitud;
import com.proyecto.adoptapet.model.Trabajador;
import com.proyecto.adoptapet.repository.AdoptanteRepository;
import com.proyecto.adoptapet.repository.CupoEntregaRepository;
import com.proyecto.adoptapet.repository.MascotaRepository;
import com.proyecto.adoptapet.repository.ProgramacionEntregaRepository;
import com.proyecto.adoptapet.repository.SolicitudRepository;
import com.proyecto.adoptapet.repository.TrabajadorRepository;

import jakarta.transaction.Transactional;

@Service
public class SolicitudService {

	@Autowired
	private SolicitudRepository solicitudRepo;
	@Autowired
	private AdoptanteRepository adoptanteRepo;
	@Autowired
	private TrabajadorRepository trabajadorRepo;
	@Autowired
	private MascotaRepository mascotaRepo;
	@Autowired
	private CupoEntregaRepository cupoEntregaRepo;
	@Autowired
	private ProgramacionEntregaRepository programacionEntregaRepo;

	private static final int CAPACIDAD_ENTREGAS_POR_HORARIO = 2;
	private static final int TOLERANCIA_CIERRE_ENTREGA_MINUTOS = 30;
	private static final int MAX_REPROGRAMACIONES = 2;
	private static final List<LocalTime[]> HORARIOS_ENTREGA = List.of(
			new LocalTime[] { LocalTime.of(10, 0), LocalTime.of(12, 0) },
			new LocalTime[] { LocalTime.of(14, 0), LocalTime.of(16, 0) },
			new LocalTime[] { LocalTime.of(16, 0), LocalTime.of(18, 0) });

	public List<Solicitud> listarSolicitudes() {
		return solicitudRepo.listarSolicitudes();
	}

	public List<Solicitud> listarSolicitudes(String estado) {
		if (estado == null || estado.isBlank()) {
			return listarSolicitudes();
		}
		return solicitudRepo.listarSolicitudesPorEstado(estado);
	}

	public List<Solicitud> listarPorAdoptante(Integer idAdoptante) {
		return solicitudRepo.listarHistorialPorAdoptante(idAdoptante);
	}

	public Solicitud buscarPorId(Integer idSolicitud) {
		return solicitudRepo.buscarDetallePorId(idSolicitud).orElse(null);
	}

	public List<HorarioDisponibilidad> listarDisponibilidadHorarios(LocalDate fecha) {
		if (fecha == null) {
			throw new RuntimeException("La fecha es obligatoria");
		}
		List<CupoEntrega> cupos = cupoEntregaRepo.listarPorFecha(fecha);
		LocalDateTime ahora = LocalDateTime.now();
		return HORARIOS_ENTREGA.stream().map((horario) -> {
			LocalTime horaInicio = horario[0];
			LocalTime horaFin = horario[1];
			CupoEntrega cupo = cupos.stream()
					.filter((item) -> item.getHora_inicio().equals(horaInicio) && item.getHora_fin().equals(horaFin))
					.findFirst().orElse(null);
			int capacidad = cupo == null ? CAPACIDAD_ENTREGAS_POR_HORARIO : cupo.getCapacidad();
			int reservadas = cupo == null ? 0 : cupo.getReservadas();
			int disponibles = Math.max(0, capacidad - reservadas);
			boolean vencido = !LocalDateTime.of(fecha, horaInicio).isAfter(ahora);
			return new HorarioDisponibilidad(formatearHorario(horaInicio) + " - " + formatearHorario(horaFin),
					formatearHoraValor(horaInicio), formatearHoraValor(horaFin), capacidad, reservadas, disponibles,
					disponibles > 0 && !vencido, vencido);
		}).toList();
	}

	@Transactional
	public Solicitud guardar(Solicitud solicitud) {
		Integer idAdoptante = solicitud.getAdoptante() == null ? null : solicitud.getAdoptante().getId_usuario();
		Integer idMascota = solicitud.getMascota() == null ? null : solicitud.getMascota().getId_mascota();
		Mascota mascota = validarSolicitud(idAdoptante, idMascota);
		if (solicitud.getRuta_dni() == null || solicitud.getRuta_dni().isBlank()) {
			throw new RuntimeException("El DNI es obligatorio para registrar la solicitud");
		}
		if (solicitud.getRuta_domicilio() == null || solicitud.getRuta_domicilio().isBlank()) {
			throw new RuntimeException("El comprobante de domicilio es obligatorio para registrar la solicitud");
		}

		Adoptante adoptante = adoptanteRepo.findById(idAdoptante)
				.orElseThrow(() -> new RuntimeException("Adoptante no encontrado"));

		solicitud.setAdoptante(adoptante);
		solicitud.setMascota(mascota);
		solicitud.setEstado_solicitud("PENDIENTE");
		solicitud.setFecha_registro(new java.sql.Date(System.currentTimeMillis()));
		mascota.setEst_adopcion("RESERVADO");
		mascotaRepo.save(mascota);
		return solicitudRepo.save(solicitud);
	}

	@Transactional
	public Solicitud guardarManual(Integer idAdoptante, Integer idMascota, String comentario, String motivoAdopcion,
			String tipoVivienda, String experienciaMascotas, String otrasMascotas, Integer cantidadPersonasHogar,
			String comentariosAdicionales, MultipartFile archivoDni, MultipartFile archivoDomicilio) throws Exception {

		Mascota mascota = validarSolicitud(idAdoptante, idMascota);
		validarArchivoObligatorio(archivoDni, "El DNI es obligatorio para registrar la solicitud");
		validarArchivoObligatorio(archivoDomicilio,
				"El comprobante de domicilio es obligatorio para registrar la solicitud");

		Adoptante adoptante = adoptanteRepo.findById(idAdoptante).orElseGet(() -> {
			String username = "firebase_user_" + idAdoptante;
			return adoptanteRepo.findByUsername(username).orElseGet(() -> {
				Adoptante nuevo = new Adoptante();
				nuevo.setId_usuario(idAdoptante);
				nuevo.setUsername(username);
				nuevo.setPassword("$2a$10$7R9gWjVUXb8mOnBf3H1ve.rG7M6BThL3.uMle3tT9zZJ3t13A.mPy");
				nuevo.setRol("ROLE_ADOPTANTE");
				nuevo.setActivo(true);
				nuevo.setNom_adoptante("Adoptante");
				nuevo.setApe_adoptante("Nro " + idAdoptante);
				nuevo.setDni("00000000");
				nuevo.setEmail("user_" + idAdoptante + "@mail.com");
				nuevo.setTelefono("999999999");
				nuevo.setDireccion("Dirección Temporal");
				nuevo.setFec_nacimiento(java.sql.Date.valueOf("2000-01-01"));
				return adoptanteRepo.saveAndFlush(nuevo);
			});
		});

		Solicitud solicitud = new Solicitud();
		solicitud.setAdoptante(adoptante);
		solicitud.setMascota(mascota);
		solicitud.setComentario(comentario);
		solicitud.setMotivo_adopcion(motivoAdopcion);
		solicitud.setTipo_vivienda(tipoVivienda);
		solicitud.setExperiencia_mascotas(experienciaMascotas);
		solicitud.setOtras_mascotas(otrasMascotas);
		solicitud.setCantidad_personas_hogar(cantidadPersonasHogar);
		solicitud.setComentarios_adicionales(comentariosAdicionales);
		solicitud.setRuta_dni(guardarArchivoAdjunto(archivoDni, "dni"));
		solicitud.setRuta_domicilio(guardarArchivoAdjunto(archivoDomicilio, "domicilio"));
		solicitud.setEstado_solicitud("PENDIENTE");
		solicitud.setFecha_registro(new java.sql.Date(System.currentTimeMillis()));

		mascota.setEst_adopcion("RESERVADO");
		mascotaRepo.save(mascota);
		return solicitudRepo.save(solicitud);
	}
	

	@Transactional
	public Solicitud aprobarSolicitud(Integer idSolicitud) {
		return aprobarSolicitud(idSolicitud, null, null);
	}

	@Transactional
	public Solicitud aprobarSolicitud(Integer idSolicitud, String usernameTrabajador) {
		return aprobarSolicitud(idSolicitud, usernameTrabajador, null);
	}

	@Transactional
	public Solicitud aprobarSolicitud(Integer idSolicitud, String usernameTrabajador,
			AprobarSolicitudRequest programacionRequest) {
		Solicitud solicitud = solicitudRepo.findById(idSolicitud)
				.orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
		if ("APROBADA".equals(solicitud.getEstado_solicitud())) {
			return solicitud;
		}
		if (!"PENDIENTE".equals(solicitud.getEstado_solicitud())) {
			throw new RuntimeException("Solo se pueden aprobar solicitudes pendientes");
		}
		if (solicitud.getMascota() == null || solicitud.getMascota().getId_mascota() == null) {
			throw new RuntimeException("La solicitud no tiene mascota asociada");
		}

		Integer idMascota = solicitud.getMascota().getId_mascota();
		Mascota mascota = mascotaRepo.findByIdForUpdate(idMascota)
				.orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
		if (solicitudRepo.existeSolicitudPorMascotaExcluyendo(idMascota, "APROBADA", idSolicitud)) {
			throw new RuntimeException("Esta mascota ya tiene una solicitud aprobada");
		}
		if (solicitudRepo.existeSolicitudPorMascotaExcluyendo(idMascota, "NO_ASISTIO", idSolicitud)) {
			throw new RuntimeException("Esta mascota tiene una solicitud no asistida pendiente de cierre");
		}
		if (solicitudRepo.existeSolicitudPorMascotaExcluyendo(idMascota, "FINALIZADA", idSolicitud)) {
			throw new RuntimeException("Esta mascota ya fue adoptada");
		}
		if ("ADOPTADO".equals(mascota.getEst_adopcion())) {
			throw new RuntimeException("La mascota ya fue adoptada");
		}
		if (!"SIN NOVEDADES".equals(mascota.getEst_salud())) {
			throw new RuntimeException(
					"No se puede aprobar la solicitud porque la mascota no se encuentra apta para adopcion");
		}
		validarProgramacionEntrega(programacionRequest);
		CupoEntrega cupo = reservarCupo(programacionRequest.getFecha_entrega(), programacionRequest.getHora_inicio(),
				programacionRequest.getHora_fin());

		solicitud.setEstado_solicitud("APROBADA");
		asignarTrabajadorResponsable(solicitud, usernameTrabajador);

		mascota.setEst_adopcion("RESERVADO");
		mascotaRepo.save(mascota);
		Solicitud aprobada = solicitudRepo.save(solicitud);
		programarEntrega(aprobada, cupo, programacionRequest);
		rechazarPendientesDuplicadas(idMascota, idSolicitud);
		return aprobada;
	}

	@Transactional
	public Solicitud rechazarSolicitud(Integer idSolicitud, String motivoRechazo) {
		Solicitud solicitud = solicitudRepo.findById(idSolicitud)
				.orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
		if (!"PENDIENTE".equals(solicitud.getEstado_solicitud())) {
			throw new RuntimeException("Solo se pueden rechazar solicitudes pendientes");
		}
		if (motivoRechazo == null || motivoRechazo.isBlank()) {
			throw new RuntimeException("El motivo de rechazo es obligatorio");
		}
		solicitud.setEstado_solicitud("RECHAZADA");
		solicitud.setMotivo_rechazo(motivoRechazo.trim());
		if (solicitud.getMascota() != null && solicitud.getMascota().getId_mascota() != null) {
			Integer idMascota = solicitud.getMascota().getId_mascota();
			Mascota mascota = mascotaRepo.findByIdForUpdate(idMascota)
					.orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
			actualizarEstadoMascotaTrasRechazo(mascota, idSolicitud);
		}
		return solicitudRepo.save(solicitud);
	}

	@Transactional
	public Solicitud rechazarSolicitud(Integer idSolicitud) {
		return rechazarSolicitud(idSolicitud, null);
	}

	@Transactional
	public Solicitud finalizarSolicitud(Integer idSolicitud, String usernameTrabajador) {
		Solicitud solicitud = solicitudRepo.findById(idSolicitud)
				.orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
		if (!"APROBADA".equals(solicitud.getEstado_solicitud())) {
			throw new RuntimeException("Solo se pueden finalizar solicitudes aprobadas");
		}
		if (solicitud.getActa_pdf() == null || solicitud.getActa_pdf().isBlank()) {
			throw new RuntimeException("Debe generar el acta antes de finalizar la adopcion");
		}
		if (solicitud.getRuta_acta_firmada() == null || solicitud.getRuta_acta_firmada().isBlank()) {
			throw new RuntimeException("Debe subir el acta firmada antes de finalizar la adopcion");
		}
		Integer idMascota = solicitud.getMascota() == null ? null : solicitud.getMascota().getId_mascota();
		if (idMascota == null) {
			throw new RuntimeException("La solicitud no tiene mascota asociada");
		}

		Mascota mascota = mascotaRepo.findByIdForUpdate(idMascota)
				.orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
		if (!"SIN NOVEDADES".equals(mascota.getEst_salud())) {
			throw new RuntimeException("No se puede finalizar la adopcion porque la mascota no se encuentra apta");
		}
		if (solicitudRepo.existeSolicitudPorMascotaExcluyendo(idMascota, "FINALIZADA", idSolicitud)) {
			throw new RuntimeException("Esta mascota ya tiene una adopcion finalizada");
		}
		ProgramacionEntrega programacion = programacionEntregaRepo.buscarPorSolicitud(idSolicitud)
				.orElseThrow(() -> new RuntimeException("La solicitud no tiene cita de entrega programada"));
		validarVentanaEntrega(programacion);

		asignarTrabajadorResponsable(solicitud, usernameTrabajador);
		solicitud.setEstado_solicitud("FINALIZADA");
		programacion.setEstado_programacion("COMPLETADA");
		programacionEntregaRepo.save(programacion);
		mascota.setEst_adopcion("ADOPTADO");
		mascotaRepo.save(mascota);
		return solicitudRepo.save(solicitud);
	}

	@Transactional
	public Solicitud finalizarPorContingencia(Integer idSolicitud, String usernameTrabajador,
			String motivoContingencia) {
		Solicitud solicitud = solicitudRepo.findById(idSolicitud)
				.orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
		if (!"APROBADA".equals(solicitud.getEstado_solicitud())) {
			throw new RuntimeException("Solo se pueden finalizar por contingencia solicitudes aprobadas");
		}
		if (!esAdmin(usernameTrabajador)) {
			throw new RuntimeException("Solo un administrador puede finalizar por contingencia");
		}
		if (motivoContingencia == null || motivoContingencia.isBlank()) {
			throw new RuntimeException("El motivo de contingencia es obligatorio");
		}
		if (solicitud.getActa_pdf() == null || solicitud.getActa_pdf().isBlank()) {
			throw new RuntimeException("Debe generar el acta antes de finalizar por contingencia");
		}
		if (solicitud.getRuta_acta_firmada() == null || solicitud.getRuta_acta_firmada().isBlank()) {
			throw new RuntimeException("Debe subir el acta firmada antes de finalizar por contingencia");
		}
		Integer idMascota = solicitud.getMascota() == null ? null : solicitud.getMascota().getId_mascota();
		if (idMascota == null) {
			throw new RuntimeException("La solicitud no tiene mascota asociada");
		}

		Mascota mascota = mascotaRepo.findByIdForUpdate(idMascota)
				.orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
		if (!"SIN NOVEDADES".equals(mascota.getEst_salud())) {
			throw new RuntimeException("No se puede finalizar la adopcion porque la mascota no se encuentra apta");
		}
		if (solicitudRepo.existeSolicitudPorMascotaExcluyendo(idMascota, "FINALIZADA", idSolicitud)) {
			throw new RuntimeException("Esta mascota ya tiene una adopcion finalizada");
		}
		ProgramacionEntrega programacion = programacionEntregaRepo.buscarPorSolicitud(idSolicitud)
				.orElseThrow(() -> new RuntimeException("La solicitud no tiene cita de entrega programada"));
		validarInicioEntrega(programacion);
		if (programacion.getFecha_limite_recojo() != null
				&& !LocalDateTime.now().isAfter(programacion.getFecha_limite_recojo())) {
			throw new RuntimeException("La contingencia solo aplica cuando la ventana normal de cierre ya vencio");
		}

		asignarTrabajadorResponsable(solicitud, usernameTrabajador);
		solicitud.setEstado_solicitud("FINALIZADA");
		solicitud.setMotivo_contingencia(motivoContingencia.trim());
		solicitud.setFecha_cierre_contingencia(LocalDateTime.now());
		programacion.setEstado_programacion("COMPLETADA");
		programacionEntregaRepo.save(programacion);
		mascota.setEst_adopcion("ADOPTADO");
		mascotaRepo.save(mascota);
		return solicitudRepo.save(solicitud);
	}

	@Transactional
	public Solicitud cancelarSolicitud(Integer idSolicitud, String motivoCancelacion) {
		Solicitud solicitud = solicitudRepo.findById(idSolicitud)
				.orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
		if (!List.of("APROBADA", "NO_ASISTIO").contains(solicitud.getEstado_solicitud())) {
			throw new RuntimeException("Solo se pueden cancelar solicitudes aprobadas o marcadas como no asistio");
		}
		if (motivoCancelacion == null || motivoCancelacion.isBlank()) {
			throw new RuntimeException("El motivo de cancelacion es obligatorio");
		}
		solicitud.setEstado_solicitud("CANCELADA");
		programacionEntregaRepo.buscarPorSolicitud(idSolicitud).ifPresent((programacion) -> {
			if (List.of("PROGRAMADA", "NO_ASISTIO").contains(programacion.getEstado_programacion())) {
				liberarCupo(programacion.getCupo());
			}
			programacion.setEstado_programacion("CANCELADA");
			programacion.setMotivo_cancelacion(motivoCancelacion.trim());
			programacionEntregaRepo.save(programacion);
		});
		if (solicitud.getMascota() != null && solicitud.getMascota().getId_mascota() != null) {
			Mascota mascota = mascotaRepo.findByIdForUpdate(solicitud.getMascota().getId_mascota())
					.orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
			actualizarEstadoMascotaTrasCierre(mascota, idSolicitud);
		}
		return solicitudRepo.save(solicitud);
	}

	@Transactional
	public Solicitud cancelarSolicitud(Integer idSolicitud) {
		return cancelarSolicitud(idSolicitud, null);
	}

	@Transactional
	public Solicitud marcarNoAsistio(Integer idSolicitud) {
		Solicitud solicitud = solicitudRepo.findById(idSolicitud)
				.orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
		if (!"APROBADA".equals(solicitud.getEstado_solicitud())) {
			throw new RuntimeException("Solo se pueden marcar como no asistio solicitudes aprobadas");
		}
		ProgramacionEntrega programacion = programacionEntregaRepo.buscarPorSolicitud(idSolicitud)
				.orElseThrow(() -> new RuntimeException("La solicitud no tiene cita de entrega programada"));
		if (!"PROGRAMADA".equals(programacion.getEstado_programacion())) {
			throw new RuntimeException("Solo se pueden marcar como no asistio citas programadas");
		}
		LocalDateTime fin = LocalDateTime.of(programacion.getFecha_entrega(), programacion.getHora_fin());
		if (LocalDateTime.now().isBefore(fin)) {
			throw new RuntimeException("Solo se puede marcar no asistio cuando ya termino el horario de entrega");
		}
		solicitud.setEstado_solicitud("NO_ASISTIO");
		programacion.setEstado_programacion("NO_ASISTIO");
		programacionEntregaRepo.save(programacion);
		return solicitudRepo.save(solicitud);
	}

	@Transactional
	public Solicitud reprogramarEntrega(Integer idSolicitud, AprobarSolicitudRequest programacionRequest) {
		return reprogramarEntrega(idSolicitud, programacionRequest, null);
	}

	@Transactional
	public Solicitud reprogramarEntrega(Integer idSolicitud, AprobarSolicitudRequest programacionRequest,
			String usernameTrabajador) {
		Solicitud solicitud = solicitudRepo.findById(idSolicitud)
				.orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
		if (!List.of("APROBADA", "NO_ASISTIO").contains(solicitud.getEstado_solicitud())) {
			throw new RuntimeException("Solo se pueden reprogramar solicitudes aprobadas o marcadas como no asistio");
		}
		int reprogramaciones = cantidadReprogramaciones(solicitud);
		if (reprogramaciones >= MAX_REPROGRAMACIONES && !esAdmin(usernameTrabajador)) {
			throw new RuntimeException(
					"La solicitud alcanzo el maximo de 2 reprogramaciones. Un administrador debe cancelar o autorizar una excepcion");
		}
		if (reprogramaciones >= MAX_REPROGRAMACIONES && esAdmin(usernameTrabajador) && (programacionRequest == null
				|| programacionRequest.getObservacion() == null || programacionRequest.getObservacion().isBlank())) {
			throw new RuntimeException("La reprogramacion extra requiere una observacion del administrador");
		}
		ProgramacionEntrega programacion = programacionEntregaRepo.buscarPorSolicitud(idSolicitud)
				.orElseThrow(() -> new RuntimeException("La solicitud no tiene cita de entrega programada"));
		if ("COMPLETADA".equals(programacion.getEstado_programacion())
				|| "CANCELADA".equals(programacion.getEstado_programacion())) {
			throw new RuntimeException("No se puede reprogramar una cita completada o cancelada");
		}
		validarProgramacionEntrega(programacionRequest);
		liberarCupo(programacion.getCupo());
		CupoEntrega nuevoCupo = reservarCupo(programacionRequest.getFecha_entrega(),
				programacionRequest.getHora_inicio(), programacionRequest.getHora_fin());

		programacion.setCupo(nuevoCupo);
		programacion.setFecha_entrega(programacionRequest.getFecha_entrega());
		programacion.setHora_inicio(programacionRequest.getHora_inicio());
		programacion.setHora_fin(programacionRequest.getHora_fin());
		programacion.setFecha_limite_recojo(fechaLimiteConTolerancia(programacionRequest));
		programacion.setEstado_programacion("PROGRAMADA");
		programacion.setMotivo_cancelacion(null);
		programacion.setObservacion(programacionRequest.getObservacion());
		programacionEntregaRepo.save(programacion);

		solicitud.setEstado_solicitud("APROBADA");
		solicitud.setCantidad_reprogramaciones(reprogramaciones + 1);
		solicitud.setProgramacionEntrega(programacion);
		return solicitudRepo.save(solicitud);
	}

	public Resource generarDocumento(Integer idSolicitud) throws Exception {
		return generarDocumento(idSolicitud, null);
	}

	@Transactional
	public Resource generarDocumento(Integer idSolicitud, String usernameTrabajador) throws Exception {
		Solicitud solicitud = solicitudRepo.findById(idSolicitud)
				.orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
		if (!"APROBADA".equals(solicitud.getEstado_solicitud())) {
			throw new RuntimeException("Solo se puede generar documento para solicitudes aprobadas");
		}
		asignarTrabajadorResponsable(solicitud, usernameTrabajador);

		Path carpeta = Paths.get(System.getProperty("user.dir"), "uploads", "documentos");
		Files.createDirectories(carpeta);
		String nombreArchivo = "acta_adopcion_" + idSolicitud + ".pdf";
		Path archivo = carpeta.resolve(nombreArchivo);
		construirDocumentoPdf(solicitud, archivo);
		solicitud.setActa_pdf("documentos/" + nombreArchivo);
		solicitudRepo.save(solicitud);
		return new UrlResource(archivo.toUri());
	}

	@Transactional
	public Solicitud subirActaFirmada(Integer idSolicitud, MultipartFile archivoActaFirmada) throws IOException {
		Solicitud solicitud = solicitudRepo.findById(idSolicitud)
				.orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
		if (!"APROBADA".equals(solicitud.getEstado_solicitud())) {
			throw new RuntimeException("Solo se puede subir acta firmada para solicitudes aprobadas");
		}
		if (solicitud.getActa_pdf() == null || solicitud.getActa_pdf().isBlank()) {
			throw new RuntimeException("Debe generar el acta antes de subir el acta firmada");
		}
		ProgramacionEntrega programacion = programacionEntregaRepo.buscarPorSolicitud(idSolicitud)
				.orElseThrow(() -> new RuntimeException("La solicitud no tiene cita de entrega programada"));
		validarInicioEntrega(programacion);
		validarArchivoObligatorio(archivoActaFirmada, "El acta firmada es obligatoria");
		solicitud.setRuta_acta_firmada(guardarArchivoAdjunto(archivoActaFirmada, "acta_firmada"));
		return solicitudRepo.save(solicitud);
	}

	private void validarProgramacionEntrega(AprobarSolicitudRequest request) {
		if (request == null) {
			throw new RuntimeException("Debe programar fecha y horario de entrega para aprobar la solicitud");
		}
		if (request.getFecha_entrega() == null || request.getHora_inicio() == null || request.getHora_fin() == null
				|| request.getFecha_limite_recojo() == null) {
			throw new RuntimeException("Fecha, horario y limite de recojo son obligatorios");
		}
		if (!request.getHora_fin().isAfter(request.getHora_inicio())) {
			throw new RuntimeException("La hora fin debe ser mayor a la hora de inicio");
		}
		if (!esHorarioPermitido(request.getHora_inicio(), request.getHora_fin())) {
			throw new RuntimeException("El horario de entrega debe ser 10:00-12:00, 14:00-16:00 o 16:00-18:00");
		}
		LocalDateTime inicio = LocalDateTime.of(request.getFecha_entrega(), request.getHora_inicio());
		if (LocalDateTime.now().isAfter(inicio)) {
			throw new RuntimeException("No se puede programar una entrega en un horario que ya inicio o vencio");
		}
		if (request.getFecha_limite_recojo().isBefore(inicio)) {
			throw new RuntimeException("La fecha limite de recojo no puede ser anterior al horario de entrega");
		}
	}

	private boolean esHorarioPermitido(LocalTime horaInicio, LocalTime horaFin) {
		return HORARIOS_ENTREGA.stream()
				.anyMatch((horario) -> horario[0].equals(horaInicio) && horario[1].equals(horaFin));
	}

	private String formatearHoraValor(LocalTime hora) {
		return String.format("%02d:%02d", hora.getHour(), hora.getMinute());
	}

	private String formatearHorario(LocalTime hora) {
		int hora12 = hora.getHour() % 12 == 0 ? 12 : hora.getHour() % 12;
		String sufijo = hora.getHour() >= 12 ? "p.m." : "a.m.";
		return String.format("%d:%02d %s", hora12, hora.getMinute(), sufijo);
	}

	private void validarVentanaEntrega(ProgramacionEntrega programacion) {
		LocalDateTime inicio = LocalDateTime.of(programacion.getFecha_entrega(), programacion.getHora_inicio());
		LocalDateTime ahora = LocalDateTime.now();
		if (ahora.isBefore(inicio)) {
			throw new RuntimeException("No se puede finalizar antes del horario programado de entrega");
		}
		if (programacion.getFecha_limite_recojo() != null && ahora.isAfter(programacion.getFecha_limite_recojo())) {
			throw new RuntimeException("La fecha limite de recojo vencio. Cancela la adopcion indicando el motivo");
		}
	}

	private void validarInicioEntrega(ProgramacionEntrega programacion) {
		LocalDateTime inicio = LocalDateTime.of(programacion.getFecha_entrega(), programacion.getHora_inicio());
		if (LocalDateTime.now().isBefore(inicio)) {
			throw new RuntimeException("El acta firmada solo se puede subir desde el inicio de la cita programada");
		}
	}

	private CupoEntrega reservarCupo(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
		CupoEntrega cupo = cupoEntregaRepo.buscarCupoParaActualizar(fecha, horaInicio, horaFin)
				.orElseGet(() -> crearCupo(fecha, horaInicio, horaFin));
		if (cupo.getReservadas() >= cupo.getCapacidad()) {
			throw new RuntimeException("Este horario ya alcanzo el limite de entregas. Selecciona otro rango");
		}
		cupo.setReservadas(cupo.getReservadas() + 1);
		return cupoEntregaRepo.save(cupo);
	}

	private CupoEntrega crearCupo(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
		CupoEntrega cupo = new CupoEntrega();
		cupo.setFecha_entrega(fecha);
		cupo.setHora_inicio(horaInicio);
		cupo.setHora_fin(horaFin);
		cupo.setCapacidad(CAPACIDAD_ENTREGAS_POR_HORARIO);
		cupo.setReservadas(0);
		try {
			return cupoEntregaRepo.saveAndFlush(cupo);
		} catch (DataIntegrityViolationException e) {
			return cupoEntregaRepo.buscarCupoParaActualizar(fecha, horaInicio, horaFin)
					.orElseThrow(() -> new RuntimeException("No se pudo reservar el horario de entrega"));
		}
	}

	private void liberarCupo(CupoEntrega cupo) {
		CupoEntrega bloqueado = cupoEntregaRepo
				.buscarCupoParaActualizar(cupo.getFecha_entrega(), cupo.getHora_inicio(), cupo.getHora_fin())
				.orElse(cupo);
		bloqueado.setReservadas(Math.max(0, bloqueado.getReservadas() - 1));
		cupoEntregaRepo.save(bloqueado);
	}

	private void programarEntrega(Solicitud solicitud, CupoEntrega cupo, AprobarSolicitudRequest request) {
		ProgramacionEntrega programacion = new ProgramacionEntrega();
		programacion.setSolicitud(solicitud);
		programacion.setCupo(cupo);
		programacion.setFecha_entrega(request.getFecha_entrega());
		programacion.setHora_inicio(request.getHora_inicio());
		programacion.setHora_fin(request.getHora_fin());
		programacion.setFecha_limite_recojo(fechaLimiteConTolerancia(request));
		programacion.setEstado_programacion("PROGRAMADA");
		programacion.setObservacion(request.getObservacion());
		programacionEntregaRepo.save(programacion);
		solicitud.setProgramacionEntrega(programacion);
	}

	private LocalDateTime fechaLimiteConTolerancia(AprobarSolicitudRequest request) {
		LocalDateTime limiteMinimo = LocalDateTime.of(request.getFecha_entrega(), request.getHora_fin())
				.plusMinutes(TOLERANCIA_CIERRE_ENTREGA_MINUTOS);
		if (request.getFecha_limite_recojo().isBefore(limiteMinimo)) {
			return limiteMinimo;
		}
		return request.getFecha_limite_recojo();
	}

	private void asignarTrabajadorResponsable(Solicitud solicitud, String usernameTrabajador) {
		if (solicitud.getTrabajador() != null) {
			return;
		}
		Trabajador trabajador = null;
		if (usernameTrabajador != null && !usernameTrabajador.isBlank()) {
			trabajador = trabajadorRepo.findByUsername(usernameTrabajador).orElse(null);
		}
		if (trabajador == null) {
			trabajador = trabajadorRepo.findPrimerActivo().orElse(null);
		}
		solicitud.setTrabajador(trabajador);
	}

	private boolean esAdmin(String usernameTrabajador) {
		if (usernameTrabajador == null || usernameTrabajador.isBlank()) {
			return false;
		}
		return trabajadorRepo.findByUsername(usernameTrabajador)
				.map((trabajador) -> "ROLE_ADMIN".equals(trabajador.getRol())).orElse(false);
	}

	private int cantidadReprogramaciones(Solicitud solicitud) {
		return solicitud.getCantidad_reprogramaciones() == null ? 0 : solicitud.getCantidad_reprogramaciones();
	}

	private Mascota validarSolicitud(Integer idAdoptante, Integer idMascota) {
		if (idAdoptante == null || idMascota == null) {
			throw new RuntimeException("Adoptante y mascota son obligatorios");
		}
		Mascota mascota = mascotaRepo.findByIdForUpdate(idMascota)
				.orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
		if (!"DISPONIBLE".equals(mascota.getEst_adopcion())) {
			throw new RuntimeException("La mascota no esta disponible para adopcion");
		}
		if (!"SIN NOVEDADES".equals(mascota.getEst_salud())) {
			throw new RuntimeException("La mascota no se encuentra apta para adopcion");
		}
		if (solicitudRepo.existeSolicitudActiva(idAdoptante, idMascota, "PENDIENTE")) {
			throw new RuntimeException("Ya existe una solicitud pendiente para esta mascota");
		}
		if (solicitudRepo.existeSolicitudPorMascotaYEstadosExcluyendo(idMascota,
				List.of("APROBADA", "NO_ASISTIO", "FINALIZADA"), 0)) {
			throw new RuntimeException("La mascota ya tiene una solicitud aprobada, no asistida o finalizada");
		}
		return mascota;
	}

	private void rechazarPendientesDuplicadas(Integer idMascota, Integer idSolicitudAprobada) {
		List<Solicitud> pendientes = solicitudRepo.listarPorMascotaYEstadoExcluyendo(idMascota, "PENDIENTE",
				idSolicitudAprobada);
		pendientes.forEach((pendiente) -> pendiente.setEstado_solicitud("RECHAZADA"));
		solicitudRepo.saveAll(pendientes);
	}

	private void actualizarEstadoMascotaTrasRechazo(Mascota mascota, Integer idSolicitudRechazada) {
		actualizarEstadoMascotaTrasCierre(mascota, idSolicitudRechazada);
	}

	private void actualizarEstadoMascotaTrasCierre(Mascota mascota, Integer idSolicitudCerrada) {
		Integer idMascota = mascota.getId_mascota();
		if (solicitudRepo.existeSolicitudPorMascotaExcluyendo(idMascota, "FINALIZADA", idSolicitudCerrada)) {
			mascota.setEst_adopcion("ADOPTADO");
		} else if (solicitudRepo.existeSolicitudPorMascotaYEstadosExcluyendo(idMascota,
				List.of("PENDIENTE", "APROBADA", "NO_ASISTIO"), idSolicitudCerrada)) {
			mascota.setEst_adopcion("RESERVADO");
		} else {
			mascota.setEst_adopcion("DISPONIBLE");
		}
		mascotaRepo.save(mascota);
	}

	private String guardarArchivoAdjunto(MultipartFile archivo, String prefijo) throws IOException {
		if (archivo == null || archivo.isEmpty()) {
			return null;
		}
		Path carpeta = Paths.get(System.getProperty("user.dir"), "uploads", "solicitudes");
		Files.createDirectories(carpeta);
		String nombreOriginal = archivo.getOriginalFilename() == null ? "archivo" : archivo.getOriginalFilename();
		String nombreFinal = prefijo + "_" + UUID.randomUUID() + "_" + nombreOriginal;
		Path destino = carpeta.resolve(nombreFinal);
		archivo.transferTo(destino.toFile());
		return "solicitudes/" + nombreFinal;
	}

	private void validarArchivoObligatorio(MultipartFile archivo, String mensaje) {
		if (archivo == null || archivo.isEmpty()) {
			throw new RuntimeException(mensaje);
		}
	}

	private void construirDocumentoPdf(Solicitud solicitud, Path archivo) throws IOException {
		Adoptante adoptante = solicitud.getAdoptante();
		Mascota mascota = solicitud.getMascota();
		Trabajador trabajador = solicitud.getTrabajador();
		PDFont regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
		PDFont bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

		try (PDDocument document = new PDDocument()) {
			PdfWriter writer = new PdfWriter(document, regular, bold);
			try {
				writer.start();
				writer.header();
				writer.badge("Acta generada el " + LocalDate.now());

				writer.section("Datos del adoptante");
				writer.field("Nombre", valor(adoptante.getNom_adoptante()) + " " + valor(adoptante.getApe_adoptante()));
				writer.field("DNI", valor(adoptante.getDni()));
				writer.field("Telefono", valor(adoptante.getTelefono()));
				writer.field("Email", valor(adoptante.getEmail()));
				writer.field("Direccion", valor(adoptante.getDireccion()));
				writer.closeSection();

				writer.section("Datos de la mascota");
				writer.field("Nombre", valor(mascota.getNombre()));
				writer.field("Especie", valor(mascota.getEspecie()));
				writer.field("Raza", valor(mascota.getRaza()));
				writer.field("Edad",
						valor(mascota.getEdad_anios()) + " anios y " + valor(mascota.getEdad_meses()) + " meses");
				writer.field("Estado de salud", valor(mascota.getEst_salud()));
				writer.closeSection();

				writer.section("Respuestas del formulario");
				writer.field("Motivo de adopcion", valor(solicitud.getMotivo_adopcion()));
				writer.field("Tipo de vivienda", valor(solicitud.getTipo_vivienda()));
				writer.field("Experiencia con mascotas", valor(solicitud.getExperiencia_mascotas()));
				writer.field("Otras mascotas", valor(solicitud.getOtras_mascotas()));
				writer.field("Personas en el hogar", valor(solicitud.getCantidad_personas_hogar()));
				writer.field("Comentarios", valor(solicitud.getComentarios_adicionales()));
				writer.closeSection();

				writer.section("Trabajador responsable");
				if (trabajador == null) {
					writer.field("Responsable", "Pendiente de asignacion");
				} else {
					writer.field("Nombre",
							valor(trabajador.getNom_trabajador()) + " " + valor(trabajador.getApe_trabajador()));
					writer.field("DNI", valor(trabajador.getDni()));
					writer.field("Email", valor(trabajador.getEmail()));
				}
				writer.closeSection();

				writer.commitment();
				writer.signatures();
			} finally {
				writer.close();
			}
			document.save(archivo.toFile());
		}
	}

	private String valor(Object valor) {
		return valor == null ? "" : valor.toString();
	}

	private static class PdfWriter {
		private static final float MARGIN = 54;
		private static final float PAGE_WIDTH = PDRectangle.LETTER.getWidth();
		private static final float PAGE_HEIGHT = PDRectangle.LETTER.getHeight();
		private static final float CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2);
		private static final float BOTTOM_MARGIN = 54;

		private final PDDocument document;
		private final PDFont regular;
		private final PDFont bold;
		private PDPageContentStream content;
		private float y = PAGE_HEIGHT - MARGIN;

		PdfWriter(PDDocument document, PDFont regular, PDFont bold) {
			this.document = document;
			this.regular = regular;
			this.bold = bold;
		}

		void start() throws IOException {
			newPage(false);
		}

		void close() throws IOException {
			if (content != null) {
				content.close();
				content = null;
			}
		}

		void header() throws IOException {
			nonStrokingColor(31, 122, 109);
			content.addRect(MARGIN, y - 40, 42, 42);
			content.fill();
			nonStrokingColor(255, 255, 255);
			writeAt("AP", MARGIN + 10, y - 27, bold, 16);

			nonStrokingColor(23, 32, 42);
			writeAt("AdoptaPet", MARGIN + 54, y - 8, bold, 14);
			nonStrokingColor(100, 116, 139);
			writeAt("Acta formal de adopcion de mascota", MARGIN + 54, y - 25, regular, 10);

			nonStrokingColor(23, 32, 42);
			writeAt("ACTA DE ADOPCION", MARGIN, y - 72, bold, 19);
			nonStrokingColor(71, 85, 105);
			writeAt("Documento generado por el portal administrativo", MARGIN, y - 89, regular, 10);

			strokingColor(31, 122, 109);
			content.setLineWidth(1.2f);
			content.moveTo(MARGIN, y - 105);
			content.lineTo(PAGE_WIDTH - MARGIN, y - 105);
			content.stroke();
			strokingColor(0, 0, 0);
			y -= 130;
		}

		void badge(String text) throws IOException {
			nonStrokingColor(241, 245, 249);
			roundedBox(MARGIN, y - 20, CONTENT_WIDTH, 28);
			content.fill();
			nonStrokingColor(71, 85, 105);
			writeAt(text, MARGIN + 12, y - 12, regular, 9.5f);
			y -= 42;
		}

		void section(String text) throws IOException {
			ensureSpace(42);
			nonStrokingColor(31, 122, 109);
			writeAt(text.toUpperCase(), MARGIN, y, bold, 11);
			y -= 14;
			strokingColor(219, 228, 236);
			content.setLineWidth(0.6f);
			content.moveTo(MARGIN, y);
			content.lineTo(PAGE_WIDTH - MARGIN, y);
			content.stroke();
			strokingColor(0, 0, 0);
			y -= 12;
		}

		void field(String label, String value) throws IOException {
			List<String> lines = wrap(value, regular, 9.5f, CONTENT_WIDTH - 128 - 18);
			float fieldHeight = Math.max(24, 16 + (lines.size() * 12));
			ensureSpace(fieldHeight + 4);
			float startY = y;
			float labelWidth = 160;
			nonStrokingColor(248, 250, 252);
			roundedBox(MARGIN, y - fieldHeight + 7, CONTENT_WIDTH, fieldHeight);
			content.fill();

			nonStrokingColor(100, 116, 139);
			writeAt(label + ":", MARGIN + 10, y - 8, bold, 9.5f);

			nonStrokingColor(51, 65, 85);
			float textY = y - 8;
			for (String line : lines) {
				writeAt(line, MARGIN + labelWidth, textY, regular, 9.5f);
				textY -= 12;
			}
			y = Math.min(startY - fieldHeight - 4, textY - 6);
		}

		void closeSection() {
			y -= 8;
		}

		void commitment() throws IOException {
			ensureSpace(120);
			section("Compromiso");
			nonStrokingColor(240, 253, 250);
			roundedBox(MARGIN, y - 52, CONTENT_WIDTH, 60);
			content.fill();
			nonStrokingColor(51, 65, 85);
			float textY = y - 12;
			String text = "El adoptante declara asumir la responsabilidad del cuidado, alimentacion, salud y bienestar de la mascota adoptada, comprometiendose a brindarle un ambiente seguro y adecuado.";
			for (String line : wrap(text, regular, 9.8f, CONTENT_WIDTH - 24)) {
				writeAt(line, MARGIN + 12, textY, regular, 9.8f);
				textY -= 13;
			}
			y -= 76;
		}

		void signatures() throws IOException {
			ensureSpace(90);
			y -= 34;
			float lineWidth = 190;
			float firstX = MARGIN + 18;
			float secondX = PAGE_WIDTH - MARGIN - lineWidth - 18;
			strokingColor(71, 85, 105);
			content.setLineWidth(0.8f);
			content.moveTo(firstX, y);
			content.lineTo(firstX + lineWidth, y);
			content.moveTo(secondX, y);
			content.lineTo(secondX + lineWidth, y);
			content.stroke();
			strokingColor(0, 0, 0);
			y -= 15;
			nonStrokingColor(71, 85, 105);
			writeAt("Firma del adoptante", firstX + 40, y, regular, 9.5f);
			writeAt("Firma del trabajador responsable", secondX + 16, y, regular, 9.5f);
		}

		private void ensureSpace(float requiredHeight) throws IOException {
			if (y - requiredHeight < BOTTOM_MARGIN) {
				newPage(true);
			}
		}

		private void newPage(boolean continuation) throws IOException {
			close();
			PDPage page = new PDPage(PDRectangle.LETTER);
			document.addPage(page);
			content = new PDPageContentStream(document, page);
			y = PAGE_HEIGHT - MARGIN;
			if (continuation) {
				miniHeader();
			}
		}

		private void miniHeader() throws IOException {
			nonStrokingColor(31, 122, 109);
			writeAt("AdoptaPet", MARGIN, y, bold, 11);
			nonStrokingColor(100, 116, 139);
			writeAt("Acta de adopcion", MARGIN + 92, y, regular, 9.5f);
			y -= 18;
			strokingColor(219, 228, 236);
			content.setLineWidth(0.6f);
			content.moveTo(MARGIN, y);
			content.lineTo(PAGE_WIDTH - MARGIN, y);
			content.stroke();
			strokingColor(0, 0, 0);
			y -= 22;
		}

		private void nonStrokingColor(int red, int green, int blue) throws IOException {
			content.setNonStrokingColor(red / 255f, green / 255f, blue / 255f);
		}

		private void strokingColor(int red, int green, int blue) throws IOException {
			content.setStrokingColor(red / 255f, green / 255f, blue / 255f);
		}

		private void writeAt(String text, float x, float currentY, PDFont font, float size) throws IOException {
			content.beginText();
			content.setFont(font, size);
			content.setRenderingMode(RenderingMode.FILL);
			content.newLineAtOffset(x, currentY);
			content.showText(sanitize(text));
			content.endText();
		}

		private List<String> wrap(String text, PDFont font, float size, float maxWidth) throws IOException {
			String safeText = sanitize(text);
			java.util.ArrayList<String> lines = new java.util.ArrayList<>();
			StringBuilder line = new StringBuilder();
			for (String word : safeText.split(" ")) {
				String candidate = line.isEmpty() ? word : line + " " + word;
				float textWidth = font.getStringWidth(candidate) / 1000 * size;
				if (textWidth > maxWidth && !line.isEmpty()) {
					lines.add(line.toString());
					line = new StringBuilder(word);
				} else {
					line = new StringBuilder(candidate);
				}
			}
			lines.add(line.toString());
			return lines;
		}

		private void roundedBox(float x, float bottomY, float width, float height) throws IOException {
			content.addRect(x, bottomY, width, height);
		}

		private String sanitize(String text) {
			return text == null ? "" : text.replace("\n", " ").replace("\r", " ");
		}
	}
}
