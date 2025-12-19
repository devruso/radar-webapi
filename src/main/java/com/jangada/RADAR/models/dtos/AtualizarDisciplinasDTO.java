package com.jangada.RADAR.models.dtos;

import java.util.Set;

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
@Schema(description = "Lista de disciplinas concluídas pelo usuário")
public class AtualizarDisciplinasDTO {
    
    @NotNull(message = "Lista de disciplinas é obrigatória")
    private Set<String> disciplinasFeitas;

}
