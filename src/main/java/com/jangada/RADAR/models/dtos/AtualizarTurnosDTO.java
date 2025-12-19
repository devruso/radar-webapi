package com.jangada.RADAR.models.dtos;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Turnos disponíveis do usuário (índices: matutino, vespertino, noturno)")
public class AtualizarTurnosDTO {
    
    @NotNull(message = "Lista de turnos é obrigatória")
    private List<Boolean> turnosLivres;

}
