package com.jangada.RADAR.models.dtos;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SalvarSimulacaoGradeDTO(
    @NotNull Long usuarioId,
    @NotBlank @Size(max = 120) String nome,
    @NotBlank String metodo,
    @NotEmpty @Size(max = 8) List<Long> turmaIds
) {
}
