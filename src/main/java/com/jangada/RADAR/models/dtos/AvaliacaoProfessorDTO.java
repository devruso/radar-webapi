package com.jangada.RADAR.models.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvaliacaoProfessorDTO {

    private Long id;
    private Long usuarioId;
    private String professorNome;
    private Long componenteId;
    private Integer nota;
    private String comentario;
    private LocalDateTime dataAvaliacao;
}
