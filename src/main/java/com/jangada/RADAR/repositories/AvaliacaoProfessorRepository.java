package com.jangada.RADAR.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jangada.RADAR.models.entities.AvaliacaoProfessor;

@Repository
public interface AvaliacaoProfessorRepository extends JpaRepository<AvaliacaoProfessor, Long> {

    List<AvaliacaoProfessor> findByProfessorNome(String professorNome);

    List<AvaliacaoProfessor> findByUsuarioId(Long usuarioId);

    Optional<AvaliacaoProfessor> findByUsuarioIdAndProfessorNomeAndComponenteId(
            Long usuarioId, String professorNome, Long componenteId);

    List<AvaliacaoProfessor> findByProfessorNomeAndComponenteId(String professorNome, Long componenteId);
}
