package com.proyecto.adoptapet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyecto.adoptapet.model.Usuario;


public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{

	Optional<Usuario> findByUsername(String username);
	
	//Validar que no se repita el nombre de usuario en el registro
    Boolean existsByUsername(String username);

	@Query("select count(u) from Usuario u where u.rol = :rol and u.activo = true")
	long countActiveByRol(@Param("rol") String rol);

	@Query(value = "select * from usuario where rol = :rol order by id_usuario asc limit 1", nativeQuery = true)
	Optional<Usuario> findPrimerUsuarioPorRol(@Param("rol") String rol);
}
