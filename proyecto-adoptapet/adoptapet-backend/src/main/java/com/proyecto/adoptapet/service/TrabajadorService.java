package com.proyecto.adoptapet.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.proyecto.adoptapet.model.Trabajador;
import com.proyecto.adoptapet.model.Usuario;
import com.proyecto.adoptapet.repository.TrabajadorRepository;
import com.proyecto.adoptapet.repository.UsuarioRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrabajadorService {

	private final TrabajadorRepository trabajadorRepo;
	private final UsuarioRepository usuarioRepo;
	private final PasswordEncoder passwordEncoder;

	public List<Trabajador> listar() {
		return trabajadorRepo.findAll();
	}

	public Trabajador buscarPorId(Integer id) {
		return trabajadorRepo.findById(id).orElse(null);
	}

	public Trabajador crear(Trabajador trabajador) {
		if (usuarioRepo.existsByUsername(trabajador.getUsername())) {
			throw new RuntimeException("El username ya existe");
		}
		if (trabajadorRepo.existsByEmail(trabajador.getEmail())) {
			throw new RuntimeException("El email ya existe");
		}
		if (trabajadorRepo.existsByDni(trabajador.getDni())) {
			throw new RuntimeException("El DNI ya existe");
		}
		trabajador.setRol("ROLE_TRABAJADOR");
		trabajador.setActivo(true);
		trabajador.setPassword(passwordEncoder.encode(trabajador.getPassword()));
		return trabajadorRepo.save(trabajador);
	}

	@Transactional
	public Trabajador actualizar(Integer id, Trabajador cambios) {
		Trabajador trabajador = trabajadorRepo.findById(id)
				.orElseThrow(() -> new RuntimeException("Trabajador no encontrado"));
		trabajador.setNom_trabajador(cambios.getNom_trabajador());
		trabajador.setApe_trabajador(cambios.getApe_trabajador());
		trabajador.setDni(cambios.getDni());
		trabajador.setFec_nacimiento(cambios.getFec_nacimiento());
		trabajador.setEmail(cambios.getEmail());
		trabajador.setTelefono(cambios.getTelefono());
		return trabajadorRepo.save(trabajador);
	}

	@Transactional
	public Trabajador eliminarLogico(Integer id) {
		Trabajador trabajador = trabajadorRepo.findById(id)
				.orElseThrow(() -> new RuntimeException("Trabajador no encontrado"));
		if ("ROLE_ADMIN".equals(trabajador.getRol())) {
			Usuario primerAdmin = usuarioRepo.findPrimerUsuarioPorRol("ROLE_ADMIN").orElse(null);
			if (primerAdmin != null && primerAdmin.getId_usuario().equals(id)) {
				throw new RuntimeException("No se puede desactivar el administrador principal");
			}
			if (usuarioRepo.countActiveByRol("ROLE_ADMIN") <= 1) {
				throw new RuntimeException("Debe existir al menos un administrador activo");
			}
		}
		trabajador.setActivo(false);
		return trabajadorRepo.save(trabajador);
	}

	@Transactional
	public Trabajador activar(Integer id) {
		Trabajador trabajador = trabajadorRepo.findById(id)
				.orElseThrow(() -> new RuntimeException("Trabajador no encontrado"));
		trabajador.setActivo(true);
		return trabajadorRepo.save(trabajador);
	}
}
