package com.jangada.RADAR.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jangada.RADAR.models.entities.PreferenciasUsuario;

@Repository
public interface PreferenciasUsuarioRepository extends JpaRepository<PreferenciasUsuario, Long> {
    
    Optional<PreferenciasUsuario> findByUsuarioId(Long usuarioId);
    
    boolean existsByUsuarioId(Long usuarioId);

}
