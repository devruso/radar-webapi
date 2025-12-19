package com.jangada.RADAR.repositories;

import com.jangada.RADAR.models.entities.AvaliacaoProfessor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvaliacaoProfessorRepository extends JpaRepository<AvaliacaoProfessor, Long> {

    List<AvaliacaoProfessor> findByProfessorNome(String professorNome);

    List<AvaliacaoProfessor> findByUsuarioId(Long usuarioId);

    Optional<AvaliacaoProfessor> findByUsuarioIdAndProfessorNomeAndComponenteId(
            Long usuarioId, String professorNome, Long componenteId);

    List<AvaliacaoProfessor> findByProfessorNomeAndComponenteId(String professorNome, Long componenteId);
}
