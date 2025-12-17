package com.jangada.RADAR.services;

import com.jangada.RADAR.models.dtos.TurmaDTO;

import java.util.List;

public interface RecomendacaoService {

    /**
     * Gera uma lista de turmas recomendadas para o usuário informado.
     * método: "burrinho" ou "busca"
     */
    List<TurmaDTO> recomendar(Long usuarioId, String metodo);

}
