package com.jangada.RADAR.repositories;

import java.util.List;
import java.util.Optional;

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
           "LEFT JOIN FETCH t.horario h " +
           "LEFT JOIN FETCH h.horarios " +
           "LEFT JOIN FETCH t.vagas " +
           "LEFT JOIN FETCH t.guiaMatricula")
    List<Turma> findAllWithDetails();

    @Query("SELECT DISTINCT t FROM Turma t " +
           "LEFT JOIN FETCH t.componenteCurricular " +
           "LEFT JOIN FETCH t.horario h " +
           "LEFT JOIN FETCH h.horarios " +
           "LEFT JOIN FETCH t.vagas " +
           "LEFT JOIN FETCH t.guiaMatricula " +
           "WHERE t.ativa = true")
    List<Turma> findAllActiveWithDetails();

    @Query("SELECT DISTINCT t FROM Turma t " +
           "LEFT JOIN FETCH t.componenteCurricular c " +
           "LEFT JOIN FETCH t.horario h " +
           "LEFT JOIN FETCH h.horarios " +
           "LEFT JOIN FETCH t.vagas " +
           "WHERE t.ativa = true AND c.id IN (" +
           "SELECT context.componenteCurricular.id FROM ComponenteCurricularContexto context " +
           "WHERE LOWER(context.courseName) = LOWER(:courseName) AND context.isActive = true)")
    List<Turma> findAllActiveByCourseName(@Param("courseName") String courseName);

    @Query("SELECT DISTINCT t FROM Turma t " +
           "LEFT JOIN FETCH t.componenteCurricular " +
           "LEFT JOIN FETCH t.horario h " +
           "LEFT JOIN FETCH h.horarios " +
           "LEFT JOIN FETCH t.vagas " +
           "LEFT JOIN FETCH t.guiaMatricula " +
           "WHERE t.id = :id")
    java.util.Optional<Turma> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT t FROM Turma t " +
           "LEFT JOIN FETCH t.componenteCurricular " +
           "LEFT JOIN FETCH t.horario h " +
           "LEFT JOIN FETCH h.horarios " +
           "LEFT JOIN FETCH t.vagas " +
           "LEFT JOIN FETCH t.guiaMatricula " +
           "WHERE t.id IN :ids")
    List<Turma> findAllByIdWithDetails(@Param("ids") List<Long> ids);

    List<Turma> findAllBySourceAndExternalKeyIn(String source, List<String> externalKeys);

    List<Turma> findAllBySourceAndPeriodoLetivoAndAtivaTrue(String source, String periodoLetivo);

    Optional<Turma> findBySourceAndExternalKey(String source, String externalKey);
    
    @Query("SELECT DISTINCT t FROM Turma t " +
           "LEFT JOIN FETCH t.componenteCurricular " +
           "LEFT JOIN FETCH t.horario h " +
           "LEFT JOIN FETCH h.horarios " +
           "LEFT JOIN FETCH t.vagas " +
           "LEFT JOIN FETCH t.guiaMatricula " +
           "WHERE t.componenteCurricular.id = :componenteId AND t.ativa = true")
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

