package com.jangada.RADAR.models.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AtualizarPerfilDTO(
    @NotBlank @Size(max = 255) String nome,
    @NotBlank @Email @Size(max = 255) String email,
    String senhaAtual,
    @Size(min = 8, max = 100) String novaSenha,
    @NotNull @Min(1) Integer perfilInicial,
    @NotNull @Min(0) Integer periodosRegularesCursados,
    @NotNull @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal coeficienteRendimento,
    @NotNull Boolean statusFormando
) {
}
