package com.jangada.RADAR.repositories;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jangada.RADAR.models.entities.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByEmailIgnoreCase(String email);
    
    @Query("SELECT DISTINCT u FROM Usuario u " +
           "LEFT JOIN FETCH u.curso c " +
           "LEFT JOIN FETCH c.estruturaCurso " +
           "LEFT JOIN FETCH c.guiaMatricula " +
           "LEFT JOIN FETCH u.turnosLivres " +
           "LEFT JOIN FETCH u.professoresExcluidos " +
           "LEFT JOIN FETCH u.disciplinasFeitas " +
           "LEFT JOIN FETCH u.turmasSelecionadas " +
           "WHERE u.id = :id")
    Optional<Usuario> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT u FROM Usuario u " +
           "LEFT JOIN FETCH u.curso " +
           "LEFT JOIN FETCH u.turnosLivres " +
           "LEFT JOIN FETCH u.professoresExcluidos " +
           "LEFT JOIN FETCH u.disciplinasFeitas " +
           "LEFT JOIN FETCH u.turmasSelecionadas")
    List<Usuario> findAllWithDetails();
}
