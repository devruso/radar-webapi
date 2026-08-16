package com.jangada.RADAR.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jangada.RADAR.models.entities.AvaliacaoProfessor;

@Repository
public interface AvaliacaoProfessorRepository extends JpaRepository<AvaliacaoProfessor, Long> {

    interface ProfessorScoreProjection {
        String getProfessorNome();
        Long getComponenteId();
        Double getScore();
    }

    @Query("SELECT a.professorNome AS professorNome, a.componente.id AS componenteId, " +
           "AVG(a.nota) AS score FROM AvaliacaoProfessor a " +
           "WHERE a.componente.id IN :componentIds " +
           "GROUP BY a.professorNome, a.componente.id")
    List<ProfessorScoreProjection> findAverageScoresByComponentIds(
        @Param("componentIds") List<Long> componentIds
    );

    Optional<AvaliacaoProfessor> findByUsuarioIdAndProfessorNomeAndComponenteId(
            Long usuarioId, String professorNome, Long componenteId);

    List<AvaliacaoProfessor> findByProfessorNomeAndComponenteId(String professorNome, Long componenteId);
}
