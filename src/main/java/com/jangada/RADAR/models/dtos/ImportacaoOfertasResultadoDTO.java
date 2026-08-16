package com.jangada.RADAR.models.dtos;

import java.util.List;

public record ImportacaoOfertasResultadoDTO(
    int recebidas,
    int criadas,
    int atualizadas,
    int desativadas,
    List<TurmaDTO> ofertas
) {
}
