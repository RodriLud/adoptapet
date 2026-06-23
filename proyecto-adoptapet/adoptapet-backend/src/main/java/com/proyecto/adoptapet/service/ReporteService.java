package com.proyecto.adoptapet.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.springframework.stereotype.Service;

import com.proyecto.adoptapet.model.Mascota;
import com.proyecto.adoptapet.model.Solicitud;
import com.proyecto.adoptapet.repository.MascotaRepository;
import com.proyecto.adoptapet.repository.SolicitudRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReporteService {

	private final MascotaRepository mascotaRepo;
	private final SolicitudRepository solicitudRepo;

	public byte[] generarReporteGeneralPdf(String estadoSolicitud, String estadoMascota, String especie) throws IOException {
		List<Mascota> mascotas = mascotaRepo.findAll();
		List<Solicitud> solicitudes = solicitudRepo.listarSolicitudes();
		mascotas = filtrarMascotas(mascotas, estadoMascota, especie);
		solicitudes = filtrarSolicitudes(solicitudes, estadoSolicitud);
		PDFont regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
		PDFont bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

		try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			ReportePdfWriter writer = new ReportePdfWriter(document, regular, bold);
			try {
				writer.start();
				writer.header();
				writer.summary(mascotas, solicitudes);
				writer.section("Mascotas por estado");
				writer.summaryRows(groupMascotas(mascotas, "estado"));
				writer.section("Mascotas por especie");
				writer.summaryRows(groupMascotas(mascotas, "especie"));
				writer.section("Solicitudes por estado");
				writer.summaryRows(groupSolicitudes(solicitudes));
				writer.section("Detalle de solicitudes");
				writer.solicitudesTable(solicitudes);
			} finally {
				writer.close();
			}
			document.save(output);
			return output.toByteArray();
		}
	}

	private List<Mascota> filtrarMascotas(List<Mascota> mascotas, String estadoMascota, String especie) {
		return mascotas.stream()
				.filter((mascota) -> estadoMascota == null || estadoMascota.isBlank()
						|| estadoMascota.equals(mascota.getEst_adopcion()))
				.filter((mascota) -> especie == null || especie.isBlank() || especie.equals(mascota.getEspecie()))
				.toList();
	}

	private List<Solicitud> filtrarSolicitudes(List<Solicitud> solicitudes, String estadoSolicitud) {
		return solicitudes.stream()
				.filter((solicitud) -> estadoSolicitud == null || estadoSolicitud.isBlank()
						|| estadoSolicitud.equals(solicitud.getEstado_solicitud()))
				.toList();
	}

	private Map<String, Long> groupMascotas(List<Mascota> mascotas, String campo) {
		return mascotas.stream().collect(Collectors.groupingBy((mascota) -> {
			if ("especie".equals(campo)) {
				return valor(mascota.getEspecie(), "SIN ESPECIE");
			}
			return valor(mascota.getEst_adopcion(), "SIN ESTADO");
		}, LinkedHashMap::new, Collectors.counting()));
	}

	private Map<String, Long> groupSolicitudes(List<Solicitud> solicitudes) {
		return solicitudes.stream()
				.collect(Collectors.groupingBy((solicitud) -> valor(solicitud.getEstado_solicitud(), "SIN ESTADO"),
						LinkedHashMap::new, Collectors.counting()));
	}

	private String valor(Object value, String fallback) {
		return value == null || value.toString().isBlank() ? fallback : value.toString();
	}

	private static class ReportePdfWriter {
		private static final float MARGIN = 46;
		private static final float PAGE_WIDTH = PDRectangle.LETTER.getWidth();
		private static final float PAGE_HEIGHT = PDRectangle.LETTER.getHeight();
		private static final float CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2);
		private static final float BOTTOM_MARGIN = 50;

		private final PDDocument document;
		private final PDFont regular;
		private final PDFont bold;
		private PDPageContentStream content;
		private float y = PAGE_HEIGHT - MARGIN;

		ReportePdfWriter(PDDocument document, PDFont regular, PDFont bold) {
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
			content.addRect(MARGIN, y - 36, 38, 38);
			content.fill();
			nonStrokingColor(255, 255, 255);
			writeAt("AP", MARGIN + 9, y - 24, bold, 15);

			nonStrokingColor(23, 32, 42);
			writeAt("AdoptaPet", MARGIN + 50, y - 7, bold, 14);
			nonStrokingColor(100, 116, 139);
			writeAt("Portal administrativo", MARGIN + 50, y - 23, regular, 9.5f);

			nonStrokingColor(23, 32, 42);
			writeAt("REPORTE ADMINISTRATIVO DE ADOPCIONES", MARGIN, y - 68, bold, 17);
			nonStrokingColor(71, 85, 105);
			writeAt("Generado el " + LocalDate.now(), MARGIN, y - 85, regular, 9.5f);

			strokingColor(31, 122, 109);
			content.setLineWidth(1.1f);
			content.moveTo(MARGIN, y - 101);
			content.lineTo(PAGE_WIDTH - MARGIN, y - 101);
			content.stroke();
			y -= 124;
		}

		void summary(List<Mascota> mascotas, List<Solicitud> solicitudes) throws IOException {
			int aprobadas = countSolicitudes(solicitudes, "APROBADA");
			int finalizadas = countSolicitudes(solicitudes, "FINALIZADA");
			int pendientes = countSolicitudes(solicitudes, "PENDIENTE");
			int noAsistio = countSolicitudes(solicitudes, "NO_ASISTIO");
			int rechazadas = countSolicitudes(solicitudes, "RECHAZADA");
			int canceladas = countSolicitudes(solicitudes, "CANCELADA");
			int disponibles = countMascotas(mascotas, "DISPONIBLE");
			float cardWidth = (CONTENT_WIDTH - 24) / 4;
			metricCard(MARGIN, "Mascotas", mascotas.size(), "Total registradas", cardWidth);
			metricCard(MARGIN + cardWidth + 8, "Disponibles", disponibles, "Para adopcion", cardWidth);
			metricCard(MARGIN + (cardWidth + 8) * 2, "Solicitudes", solicitudes.size(), "Expedientes", cardWidth);
			metricCard(MARGIN + (cardWidth + 8) * 3, "Finalizadas", finalizadas, "Adopciones cerradas", cardWidth);
			y -= 86;

			section("Indicadores principales");
			Map<String, String> indicadores = new LinkedHashMap<>();
			indicadores.put("Pendientes", String.valueOf(pendientes));
			indicadores.put("Aprobadas pendientes de entrega", String.valueOf(aprobadas));
			indicadores.put("No asistio pendientes de cierre", String.valueOf(noAsistio));
			indicadores.put("Finalizadas", String.valueOf(finalizadas));
			indicadores.put("Rechazadas", String.valueOf(rechazadas));
			indicadores.put("Canceladas", String.valueOf(canceladas));
			indicadores.put("Tasa de finalizacion", percent(finalizadas, solicitudes.size()) + "%");
			summaryRowsText(indicadores);
		}

		void section(String title) throws IOException {
			ensureSpace(38);
			nonStrokingColor(31, 122, 109);
			writeAt(title.toUpperCase(), MARGIN, y, bold, 10.5f);
			y -= 12;
			strokingColor(219, 228, 236);
			content.setLineWidth(0.6f);
			content.moveTo(MARGIN, y);
			content.lineTo(PAGE_WIDTH - MARGIN, y);
			content.stroke();
			y -= 13;
		}

		void summaryRows(Map<String, Long> rows) throws IOException {
			for (Map.Entry<String, Long> row : rows.entrySet()) {
				summaryRow(row.getKey(), String.valueOf(row.getValue()));
			}
			y -= 10;
		}

		void summaryRowsText(Map<String, String> rows) throws IOException {
			for (Map.Entry<String, String> row : rows.entrySet()) {
				summaryRow(row.getKey(), row.getValue());
			}
			y -= 10;
		}

		private void summaryRow(String label, String value) throws IOException {
				ensureSpace(25);
				nonStrokingColor(248, 250, 252);
				content.addRect(MARGIN, y - 18, CONTENT_WIDTH, 24);
				content.fill();
				nonStrokingColor(51, 65, 85);
				writeAt(label, MARGIN + 10, y - 8, regular, 9.5f);
				writeAt(value, PAGE_WIDTH - MARGIN - 52, y - 8, bold, 9.5f);
				y -= 28;
		}

		void solicitudesTable(List<Solicitud> solicitudes) throws IOException {
			tableHeader();
			for (Solicitud solicitud : solicitudes) {
				ensureSpace(31);
				float rowTop = y;
				nonStrokingColor(255, 255, 255);
				content.addRect(MARGIN, y - 24, CONTENT_WIDTH, 28);
				content.fill();
				strokingColor(226, 232, 240);
				content.moveTo(MARGIN, y - 25);
				content.lineTo(PAGE_WIDTH - MARGIN, y - 25);
				content.stroke();
				nonStrokingColor(51, 65, 85);
				writeAt(limit(textoAdoptante(solicitud), 25), MARGIN + 6, rowTop - 10, regular, 7.8f);
				writeAt(limit(valor(solicitud.getMascota() == null ? null : solicitud.getMascota().getNombre()), 14), MARGIN + 150, rowTop - 10,
						regular, 7.8f);
				writeAt(valor(solicitud.getEstado_solicitud()), MARGIN + 238, rowTop - 10, bold, 7.8f);
				writeAt(limit(valor(solicitud.getTipo_vivienda()), 14), MARGIN + 330, rowTop - 10, regular, 7.8f);
				writeAt(valor(solicitud.getCantidad_personas_hogar()), MARGIN + 418, rowTop - 10, regular, 7.8f);
				writeAt(valor(solicitud.getFecha_registro()), MARGIN + 476, rowTop - 10, regular, 7.8f);
				y -= 29;
			}
		}

		private void tableHeader() throws IOException {
			ensureSpace(45);
			nonStrokingColor(23, 32, 42);
			content.addRect(MARGIN, y - 24, CONTENT_WIDTH, 28);
			content.fill();
			nonStrokingColor(255, 255, 255);
			writeAt("Adoptante", MARGIN + 6, y - 10, bold, 7.8f);
			writeAt("Mascota", MARGIN + 150, y - 10, bold, 7.8f);
			writeAt("Estado", MARGIN + 238, y - 10, bold, 7.8f);
			writeAt("Vivienda", MARGIN + 330, y - 10, bold, 7.8f);
			writeAt("Hogar", MARGIN + 418, y - 10, bold, 7.8f);
			writeAt("Fecha", MARGIN + 476, y - 10, bold, 7.8f);
			y -= 31;
		}

		private void metricCard(float x, String label, int value, String note, float width) throws IOException {
			nonStrokingColor(248, 250, 252);
			content.addRect(x, y - 58, width, 66);
			content.fill();
			nonStrokingColor(100, 116, 139);
			writeAt(label, x + 10, y - 10, regular, 8.5f);
			nonStrokingColor(23, 32, 42);
			writeAt(String.valueOf(value), x + 10, y - 31, bold, 17);
			nonStrokingColor(100, 116, 139);
			writeAt(note, x + 10, y - 48, regular, 7.8f);
		}

		private int countSolicitudes(List<Solicitud> solicitudes, String estado) {
			return (int) solicitudes.stream().filter((solicitud) -> estado.equals(solicitud.getEstado_solicitud())).count();
		}

		private int countMascotas(List<Mascota> mascotas, String estado) {
			return (int) mascotas.stream().filter((mascota) -> estado.equals(mascota.getEst_adopcion())).count();
		}

		private int percent(int value, int total) {
			return total == 0 ? 0 : Math.round((value * 100f) / total);
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
			writeAt("AdoptaPet", MARGIN, y, bold, 10.5f);
			nonStrokingColor(100, 116, 139);
			writeAt("Reporte administrativo", MARGIN + 80, y, regular, 9);
			y -= 16;
			strokingColor(219, 228, 236);
			content.setLineWidth(0.6f);
			content.moveTo(MARGIN, y);
			content.lineTo(PAGE_WIDTH - MARGIN, y);
			content.stroke();
			y -= 24;
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

		private String textoAdoptante(Solicitud solicitud) {
			if (solicitud.getAdoptante() == null) {
				return "Sin dato";
			}
			return valor(solicitud.getAdoptante().getNom_adoptante()) + " "
					+ valor(solicitud.getAdoptante().getApe_adoptante());
		}

		private String valor(Object value) {
			return value == null || value.toString().isBlank() ? "Sin dato" : value.toString();
		}

		private String sanitize(String text) {
			return text == null ? "" : text.replace("\n", " ").replace("\r", " ");
		}

		private String limit(String text, int maxLength) {
			String safeText = sanitize(text);
			return safeText.length() <= maxLength ? safeText : safeText.substring(0, maxLength - 3) + "...";
		}
	}
}
