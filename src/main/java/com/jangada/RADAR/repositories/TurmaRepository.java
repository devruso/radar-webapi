package com.jangada.RADAR.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jangada.RADAR.models.entities.Turma;
import java.util.List;

@Repository
public interface TurmaRepository extends JpaRepository<Turma, Long> {
    
    @Query("SELECT t FROM Turma t WHERE t.componenteCurricular.id = :componenteId")
    List<Turma> findByComponenteId(@Param("componenteId") Long componenteId);
    
    @Query("SELECT t FROM Turma t WHERE t.professor = :professor")
    List<Turma> findByProfessor(@Param("professor") String professor);
    
    @Query("SELECT t FROM Turma t WHERE t.guiaMatricula.id = :guiaId")
    List<Turma> findByGuiaId(@Param("guiaId") Long guiaId);
    
    @Query("SELECT t FROM Turma t WHERE t.tipo = :tipo")
    List<Turma> findByTipo(@Param("tipo") Byte tipo);
}

