package com.proyecto.adoptapet.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.adoptapet.model.Adoptante;
import com.proyecto.adoptapet.model.ApiResponse;
import com.proyecto.adoptapet.model.Usuario;
import com.proyecto.adoptapet.service.UsuarioService;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UsuarioService usuarioService;

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
		try {
			String username = credentials.get("username");
			String password = credentials.get("password");

			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
			Usuario usuario = usuarioService.findByUsername(username);

			return ResponseEntity.ok(usuario);
		} catch (AuthenticationException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new ApiResponse("Usuario o contrasena incorrectos"));
		}
	}

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody Adoptante newUser) {
		try {
			Adoptante savedUser = usuarioService.registrarAdoptante(newUser);
			return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(e.getMessage()));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage()));
		}
	}
}
