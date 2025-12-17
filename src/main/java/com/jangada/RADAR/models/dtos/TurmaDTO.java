package com.jangada.RADAR.models.dtos;

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
    private String local;
    private String professor;
    private String numero;
    private Byte tipo;

    private Long componenteId;
    private Long horarioId;
    private Long vagasId;
    private Long guiaId;
}
