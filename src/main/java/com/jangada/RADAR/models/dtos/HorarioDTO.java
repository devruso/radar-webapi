package com.jangada.RADAR.models.dtos;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HorarioDTO {
    private Long id;
    private String codigo;
    private String turno;
    private Map<String, String> horarios;
}
