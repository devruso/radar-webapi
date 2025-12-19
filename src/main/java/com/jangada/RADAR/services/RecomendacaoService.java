package com.jangada.RADAR.services;

import com.jangada.RADAR.models.dtos.AvaliacaoProfessorDTO;
import com.jangada.RADAR.models.dtos.RecomendacaoTurmaDTO;
import com.jangada.RADAR.models.dtos.TurmaDTO;

import java.util.List;

public interface RecomendacaoService {

    /**
     * Gera uma lista de turmas recomendadas para o usuário informado.
     * método: "burrinho" (simples filtro + ordenação) ou "busca" (futuro: algoritmo mais complexo)
     */
    List<RecomendacaoTurmaDTO> recomendar(Long usuarioId, String metodo);

    /**
     * Registra uma avaliação de professor após conclusão de disciplina.
     * Escala: 1-5 (1=ruim, 5=excelente)
     */
    AvaliacaoProfessorDTO avaliarProfessor(Long usuarioId, String professorNome, Long componenteId, Integer nota, String comentario);

    /**
     * Obtém avaliações de um professor específico
     */
    List<AvaliacaoProfessorDTO> obterAvaliacoesProfessor(String professorNome);

    /**
     * Obtém score médio de um professor
     */
    Double obterScoreProfessor(String professorNome, Long componenteId);
}

