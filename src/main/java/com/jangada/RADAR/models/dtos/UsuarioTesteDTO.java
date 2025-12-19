package com.jangada.RADAR.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados mínimos para criar usuário teste (sem cadastro)")
public class UsuarioTesteDTO {
    
    @NotNull(message = "Curso é obrigatório")
    private Long cursoId;
    
    @NotNull(message = "Ano de ingresso é obrigatório")
    @Min(value = 2000, message = "Ano de ingresso inválido")
    private Integer anoIngresso;

}
