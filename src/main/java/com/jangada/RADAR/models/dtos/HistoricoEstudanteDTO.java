package com.jangada.RADAR.models.dtos;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Histórico acadêmico do estudante")
public class HistoricoEstudanteDTO {
    
    private Long id;
    
    @NotNull(message = "ID do usuário é obrigatório")
    private Long usuarioId;
    
    @NotNull(message = "ID do componente é obrigatório")
    private Long componenteId;
    
    private String componenteNome;
    private String componenteCodigo;
    
    @NotBlank(message = "Semestre é obrigatório")
    private String semestre;
    
    private Double nota;
    
    @NotBlank(message = "Status é obrigatório")
    private String status; // APROVADO, REPROVADO, TRANCADO
    
    private LocalDate dataConclusao;

}
