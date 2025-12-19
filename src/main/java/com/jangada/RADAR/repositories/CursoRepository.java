package com.jangada.RADAR.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jangada.RADAR.models.entities.Curso;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {
    
    // Otimizado com FETCH JOIN para evitar N+1 queries
    @Query("SELECT DISTINCT c FROM Curso c " +
           "LEFT JOIN FETCH c.estruturaCurso " +
           "LEFT JOIN FETCH c.guiaMatricula")
    List<Curso> findAllWithDetails();
    
    @Query("SELECT DISTINCT c FROM Curso c " +
           "LEFT JOIN FETCH c.estruturaCurso " +
           "LEFT JOIN FETCH c.guiaMatricula " +
           "WHERE c.id = :id")
    Optional<Curso> findByIdWithDetails(@Param("id") Long id);
}
