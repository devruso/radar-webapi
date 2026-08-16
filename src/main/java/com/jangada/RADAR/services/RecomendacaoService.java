package com.jangada.RADAR.services;

import java.util.List;

import com.jangada.RADAR.models.dtos.AvaliacaoProfessorDTO;
import com.jangada.RADAR.models.dtos.RecomendacaoTurmaDTO;

public interface RecomendacaoService {

    /**
     * Gera uma lista de turmas recomendadas para o usuário informado.
     * Método "guloso" para seleção simples ou "busca" para otimização por feixe.
     */
    List<RecomendacaoTurmaDTO> recomendar(Long usuarioId, String metodo);

    /**
     * Registra uma avaliação de professor após conclusão de disciplina.
     * Escala: 1-5 (1=ruim, 5=excelente)
     */
    AvaliacaoProfessorDTO avaliarProfessor(Long usuarioId, String professorNome, Long componenteId, Integer nota, String comentario);

    /**
     * Obtém score médio de um professor
     */
    Double obterScoreProfessor(String professorNome, Long componenteId);
}

