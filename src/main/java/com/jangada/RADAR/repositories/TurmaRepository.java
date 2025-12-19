package com.jangada.RADAR.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jangada.RADAR.models.entities.Turma;

@Repository
public interface TurmaRepository extends JpaRepository<Turma, Long> {
    
    // Otimizado com FETCH JOIN para evitar N+1 queries
    @Query("SELECT DISTINCT t FROM Turma t " +
           "LEFT JOIN FETCH t.componenteCurricular " +
           "LEFT JOIN FETCH t.horario " +
           "LEFT JOIN FETCH t.vagas " +
           "LEFT JOIN FETCH t.guiaMatricula")
    List<Turma> findAllWithDetails();
    
    @Query("SELECT DISTINCT t FROM Turma t " +
           "LEFT JOIN FETCH t.componenteCurricular " +
           "LEFT JOIN FETCH t.horario " +
           "LEFT JOIN FETCH t.vagas " +
           "LEFT JOIN FETCH t.guiaMatricula " +
           "WHERE t.componenteCurricular.id = :componenteId")
    List<Turma> findByComponenteId(@Param("componenteId") Long componenteId);
    
    @Query("SELECT DISTINCT t FROM Turma t " +
           "LEFT JOIN FETCH t.componenteCurricular " +
           "LEFT JOIN FETCH t.horario " +
           "LEFT JOIN FETCH t.vagas " +
           "LEFT JOIN FETCH t.guiaMatricula " +
           "WHERE t.professor = :professor")
    List<Turma> findByProfessor(@Param("professor") String professor);
    
    @Query("SELECT DISTINCT t FROM Turma t " +
           "LEFT JOIN FETCH t.componenteCurricular " +
           "LEFT JOIN FETCH t.horario " +
           "LEFT JOIN FETCH t.vagas " +
           "LEFT JOIN FETCH t.guiaMatricula " +
           "WHERE t.guiaMatricula.id = :guiaId")
    List<Turma> findByGuiaId(@Param("guiaId") Long guiaId);
    
    @Query("SELECT DISTINCT t FROM Turma t " +
           "LEFT JOIN FETCH t.componenteCurricular " +
           "LEFT JOIN FETCH t.horario " +
           "LEFT JOIN FETCH t.vagas " +
           "LEFT JOIN FETCH t.guiaMatricula " +
           "WHERE t.tipo = :tipo")
    List<Turma> findByTipo(@Param("tipo") Byte tipo);
}

