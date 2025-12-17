package com.jangada.RADAR.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursoDTO {
    private Long id;
    private String nome;
    private String coordenador;
    private String nivel;
    private String turno;

    private Long estruturaId;
    private Long guiaId;
}
