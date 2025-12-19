package com.jangada.RADAR.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jangada.RADAR.models.entities.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByEmail(String email);
    
    // Otimizado com FETCH JOIN para evitar N+1 queries
    @Query("SELECT DISTINCT u FROM Usuario u " +
           "LEFT JOIN FETCH u.curso c " +
           "LEFT JOIN FETCH c.estruturaCurso " +
           "LEFT JOIN FETCH c.guiaMatricula " +
           "WHERE u.id = :id")
    Optional<Usuario> findByIdWithDetails(@Param("id") Long id);
}
