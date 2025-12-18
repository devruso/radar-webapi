package com.jangada.RADAR.models.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurmaDTO {
    private Long id;
    
    @NotBlank(message = "Local é obrigatório")
    private String local;
    
    @NotBlank(message = "Professor é obrigatório")
    private String professor;
    
    @NotBlank(message = "Número da turma é obrigatório")
    private String numero;
    
    @NotNull(message = "Tipo é obrigatório")
    private Byte tipo;

    @NotNull(message = "Componente curricular é obrigatório")
    private Long componenteId;
    
    private Long horarioId;
    private Long vagasId;
    private Long guiaId;
}
