package com.jangada.RADAR.models.dtos;

import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VagasDTO {
    private Long id;
    private Short totalVagas;
    private Map<String, Integer> reservaVagas;
}
