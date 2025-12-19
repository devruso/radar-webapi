package com.jangada.RADAR.models.dtos;

import java.util.List;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados do usuário do sistema RADAR")
public class UsuarioDTO {
    private Long id;
    
    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    
    @NotBlank(message = "Matrícula é obrigatória")
    private String matricula;
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    private Integer limiteMatricula;
    private Integer tempoEstudo;
    private Integer tempoTransporte;

    // UFBA: ano de ingresso (ex.: 2025.2) e período atual
    private String anoIngresso;
    private Integer periodoAtual;

    private List<Boolean> turnosLivres;
    private Set<String> professoresExcluidos;
    private Set<String> disciplinasFeitas;

    private Set<Long> turmasSelecionadasIds;
    private Long cursoId;

}
