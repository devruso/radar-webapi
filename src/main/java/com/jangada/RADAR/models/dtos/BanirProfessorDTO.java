package com.jangada.RADAR.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Professor a ser banido ou removido da lista de banidos")
public class BanirProfessorDTO {
    
    @NotBlank(message = "Nome do professor é obrigatório")
    private String professorNome;

}
