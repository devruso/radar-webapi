package com.jangada.RADAR.models.dtos;

import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuiaMatriculaDTO {
    private Long id;
    private String anoPeriodo;
    private Set<Long> turmasOferecidasIds;
    private Long cursoId;
}
