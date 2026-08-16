package com.jangada.RADAR.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jangada.RADAR.models.entities.SimulacaoGrade;

public interface SimulacaoGradeRepository extends JpaRepository<SimulacaoGrade, Long> {

    @Query("SELECT DISTINCT s FROM SimulacaoGrade s " +
           "LEFT JOIN FETCH s.usuario " +
           "LEFT JOIN FETCH s.turmas t " +
           "LEFT JOIN FETCH t.componenteCurricular " +
           "LEFT JOIN FETCH t.horario h " +
           "LEFT JOIN FETCH h.horarios " +
           "LEFT JOIN FETCH t.vagas " +
           "WHERE s.usuario.id = :usuarioId ORDER BY s.criadaEm DESC")
    List<SimulacaoGrade> findAllByUsuarioIdWithDetails(@Param("usuarioId") Long usuarioId);

    @Query("SELECT DISTINCT s FROM SimulacaoGrade s " +
           "LEFT JOIN FETCH s.usuario " +
           "LEFT JOIN FETCH s.turmas t " +
           "LEFT JOIN FETCH t.componenteCurricular " +
           "LEFT JOIN FETCH t.horario h " +
           "LEFT JOIN FETCH h.horarios " +
           "LEFT JOIN FETCH t.vagas " +
           "WHERE s.id = :id")
    Optional<SimulacaoGrade> findByIdWithDetails(@Param("id") Long id);
}
