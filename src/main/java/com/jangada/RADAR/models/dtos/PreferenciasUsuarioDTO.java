package com.jangada.RADAR.models.dtos;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Preferências do usuário (turnos e professores banidos)")
public class PreferenciasUsuarioDTO {
    
    private Long id;
    private Long usuarioId;
    
    private List<String> turnosDisponiveis; // ["MATUTINO", "VESPERTINO", "NOTURNO"]
    private List<String> professoresBanidos; // ["Prof. João", "Prof. Maria"]
    
    private LocalDateTime dataAtualizacao;

}
