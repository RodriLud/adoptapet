package com.proyecto.adoptapet.controller;

import java.time.LocalDate;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.adoptapet.service.ReporteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reporte")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ReporteController {

	private final ReporteService reporteService;

	@GetMapping("/general/pdf")
	public ResponseEntity<byte[]> generarReporteGeneral(
			@RequestParam(value = "estadoSolicitud", required = false) String estadoSolicitud,
			@RequestParam(value = "estadoMascota", required = false) String estadoMascota,
			@RequestParam(value = "especie", required = false) String especie) {
		try {
			byte[] pdf = reporteService.generarReporteGeneralPdf(estadoSolicitud, estadoMascota, especie);
			String filename = "reporte_adopciones_" + LocalDate.now() + ".pdf";
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"").body(pdf);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(new byte[0]);
		}
	}
}
