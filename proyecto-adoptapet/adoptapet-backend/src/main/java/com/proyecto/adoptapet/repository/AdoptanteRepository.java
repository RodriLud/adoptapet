package com.proyecto.adoptapet.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.adoptapet.model.Adoptante;

public interface AdoptanteRepository extends JpaRepository<Adoptante, Integer> {

    Boolean existsByEmail(String email);

    Boolean existsByDni(String dni);
    
    Optional<Adoptante> findByUsername(String username);
}