package com.jangada.RADAR.models.dtos;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record ImportarOfertasDTO(
    @NotBlank @Size(max = 100) String source,
    @NotBlank @Size(max = 20) String periodoLetivo,
    boolean substituirPeriodo,
    @NotEmpty @Size(max = 500) List<@Valid OfertaTurmaInputDTO> ofertas
) {
}
