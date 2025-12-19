package com.jangada.RADAR.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreRequisitoDTO {

    private Long id;
    private Long componenteId;
    private Long componentePreRequisitoId;
    private String tipo;
}
