package com.proyecto.adoptapet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.adoptapet.model.ApiResponse;
import com.proyecto.adoptapet.model.Trabajador;
import com.proyecto.adoptapet.service.TrabajadorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/trabajador")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class TrabajadorController {

	private final TrabajadorService service;

	@GetMapping
	public ResponseEntity<?> listar() {
		return ResponseEntity.ok(service.listar());
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> buscar(@PathVariable Integer id) {
		Trabajador trabajador = service.buscarPorId(id);
		return trabajador == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(trabajador);
	}

	@PostMapping("/registrar")
	public ResponseEntity<?> registrar(@RequestBody Trabajador trabajador) {
		try {
			return ResponseEntity.ok(service.crear(trabajador));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
		}
	}

	@PutMapping("/actualizar/{id}")
	public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody Trabajador trabajador) {
		try {
			return ResponseEntity.ok(service.actualizar(id, trabajador));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
		}
	}

	@DeleteMapping("/eliminar/{id}")
	public ResponseEntity<?> eliminar(@PathVariable Integer id) {
		try {
			return ResponseEntity.ok(service.eliminarLogico(id));
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
