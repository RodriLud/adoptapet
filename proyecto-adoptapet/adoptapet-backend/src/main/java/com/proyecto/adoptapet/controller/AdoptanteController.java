package com.proyecto.adoptapet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.adoptapet.model.ApiResponse;
import com.proyecto.adoptapet.service.AdoptanteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/adoptante")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AdoptanteController {
	private final AdoptanteService service;

	@GetMapping
	public ResponseEntity<?> listar() {
		return ResponseEntity.ok(service.listarResumenes());
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> buscarResumen(@PathVariable Integer id) {
		return ResponseEntity.ok(service.buscarResumen(id));
	}

	@PutMapping("/desactivar/{id}")
	public ResponseEntity<?> desactivar(@PathVariable Integer id) {
		try {
			return ResponseEntity.ok(service.desactivar(id));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
		}
	}

	@PutMapping("/activar/{id}")
	public ResponseEntity<?> activar(@PathVariable Integer id) {
		try {
			return ResponseEntity.ok(service.activar(id));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
		}
	}
}
