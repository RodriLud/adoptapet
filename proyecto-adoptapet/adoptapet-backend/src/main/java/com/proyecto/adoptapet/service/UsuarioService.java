package com.proyecto.adoptapet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.proyecto.adoptapet.model.Adoptante;
import com.proyecto.adoptapet.model.Usuario;
import com.proyecto.adoptapet.repository.AdoptanteRepository;
import com.proyecto.adoptapet.repository.UsuarioRepository;

@Service
public class UsuarioService implements UserDetailsService {

	@Autowired
	private UsuarioRepository repo;

	@Autowired
	private AdoptanteRepository adoptanteRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Usuario usuario = repo.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

		return User.builder()
				.username(usuario.getUsername())
				.password(usuario.getPassword())
				.disabled(Boolean.FALSE.equals(usuario.getActivo()))
				.roles(usuario.getRol().replace("ROLE_", ""))
				.build();
	}

	public Usuario findByUsername(String username) {
		return repo.findByUsername(username).orElse(null);
	}

	public Adoptante saveAdoptante(Adoptante adoptante) {
		return adoptanteRepo.save(adoptante);
	}

	public Adoptante registrarAdoptante(Adoptante adoptante) {
		validarRegistroAdoptante(adoptante);

		if (findByUsername(adoptante.getUsername()) != null) {
			throw new IllegalStateException("El username ya existe");
		}

		if (existsAdoptanteEmail(adoptante.getEmail())) {
			throw new IllegalStateException("El email ya esta registrado");
		}

		if (existsAdoptanteDni(adoptante.getDni())) {
			throw new IllegalStateException("El DNI ya esta registrado");
		}

		if (adoptante.getRol() == null || adoptante.getRol().isEmpty()) {
			adoptante.setRol("ROLE_ADOPTANTE");
		}

		adoptante.setActivo(true);
		adoptante.setPassword(passwordEncoder.encode(adoptante.getPassword()));
		return saveAdoptante(adoptante);
	}

	public Boolean existsAdoptanteEmail(String email) {
		return email != null && adoptanteRepo.existsByEmail(email);
	}

	public Boolean existsAdoptanteDni(String dni) {
		return dni != null && adoptanteRepo.existsByDni(dni);
	}

	private void validarRegistroAdoptante(Adoptante adoptante) {
		if (adoptante == null) {
			throw new IllegalArgumentException("Los datos de registro son obligatorios");
		}
		if (estaVacio(adoptante.getUsername())) {
			throw new IllegalArgumentException("El username es obligatorio");
		}
		if (adoptante.getUsername().length() > 50) {
			throw new IllegalArgumentException("El username debe tener maximo 50 caracteres");
		}
		if (estaVacio(adoptante.getPassword())) {
			throw new IllegalArgumentException("La contrasena es obligatoria");
		}
		if (estaVacio(adoptante.getNom_adoptante())) {
			throw new IllegalArgumentException("El nombre es obligatorio");
		}
		if (adoptante.getNom_adoptante().length() > 100) {
			throw new IllegalArgumentException("El nombre debe tener maximo 100 caracteres");
		}
		if (estaVacio(adoptante.getApe_adoptante())) {
			throw new IllegalArgumentException("El apellido es obligatorio");
		}
		if (adoptante.getApe_adoptante().length() > 100) {
			throw new IllegalArgumentException("El apellido debe tener maximo 100 caracteres");
		}
		if (estaVacio(adoptante.getDni())) {
			throw new IllegalArgumentException("El DNI es obligatorio");
		}
		if (adoptante.getDni().length() != 8) {
			throw new IllegalArgumentException("El DNI debe tener 8 digitos");
		}
		if (adoptante.getFec_nacimiento() == null) {
			throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
		}
		if (estaVacio(adoptante.getEmail())) {
			throw new IllegalArgumentException("El email es obligatorio");
		}
		if (adoptante.getEmail().length() > 100) {
			throw new IllegalArgumentException("El email debe tener maximo 100 caracteres");
		}
		if (estaVacio(adoptante.getTelefono())) {
			throw new IllegalArgumentException("El telefono es obligatorio");
		}
		if (adoptante.getTelefono().length() != 9) {
			throw new IllegalArgumentException("El telefono debe tener 9 digitos");
		}
		if (adoptante.getDireccion() != null && adoptante.getDireccion().length() > 150) {
			throw new IllegalArgumentException("La direccion debe tener maximo 150 caracteres");
		}
	}

	private boolean estaVacio(String valor) {
		return valor == null || valor.trim().isEmpty();
	}
}
