package com.jangada.RADAR.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
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
@Schema(description = "Dados de cadastro de novo usuário")
public class UsuarioCadastroDTO {
    
    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;
    
    @NotBlank(message = "Senha é obrigatória")
    private String senha;
    
    @NotBlank(message = "Confirmação de senha é obrigatória")
    private String confirmarSenha;
    
    @NotNull(message = "Curso é obrigatório")
    private Long cursoId;
    
    @NotNull(message = "Mês de ingresso é obrigatório")
    @Min(value = 1, message = "Mês deve ser entre 1 e 12")
    private Integer mesIngresso;
    
    @NotNull(message = "Ano de ingresso é obrigatório")
    @Min(value = 2000, message = "Ano de ingresso inválido")
    private Integer anoIngresso;

}
