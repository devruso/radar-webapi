package com.jangada.RADAR.models.dtos;

import java.util.Map;
import java.util.Set;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record OfertaTurmaInputDTO(
    @NotBlank @Size(max = 160) String externalKey,
    @NotBlank @Size(max = 50) String componenteCodigo,
    @Size(max = 255) String componenteNome,
    @NotBlank @Size(max = 50) String numero,
    @NotBlank @Size(max = 255) String professor,
    @NotBlank @Size(max = 255) String local,
    Byte tipo,
    @NotBlank @Size(max = 50) String turno,
    @NotEmpty Map<String, String> horarios,
    @Min(0) @Max(32767) Short totalVagas,
    @Min(0) @Max(32767) Short vagasDisponiveis,
    Map<String, Integer> reservas,
    Set<@NotBlank @Size(max = 50) String> equivalencias
) {
    public OfertaTurmaInputDTO(
        String externalKey,
        String componenteCodigo,
        String componenteNome,
        String numero,
        String professor,
        String local,
        Byte tipo,
        String turno,
        Map<String, String> horarios,
        Short totalVagas,
        Short vagasDisponiveis,
        Map<String, Integer> reservas
    ) {
        this(
            externalKey,
            componenteCodigo,
            componenteNome,
            numero,
            professor,
            local,
            tipo,
            turno,
            horarios,
            totalVagas,
            vagasDisponiveis,
            reservas,
            null
        );
    }
}
