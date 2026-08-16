package com.jangada.RADAR.models.dtos;

import java.time.Instant;
import java.util.List;

public record SimulacaoGradeDTO(
    Long id,
    Long usuarioId,
    String nome,
    String metodo,
    Instant criadaEm,
    List<TurmaDTO> turmas
) {
}
