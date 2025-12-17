package com.jangada.RADAR.mappers;

import com.jangada.RADAR.models.dtos.GuiaMatriculaDTO;
import com.jangada.RADAR.models.entities.GuiaMatricula;

import java.util.stream.Collectors;

public class GuiaMatriculaMapper {

    public static GuiaMatriculaDTO toDto(GuiaMatricula g) {
        if (g == null) return null;
        return GuiaMatriculaDTO.builder()
                .id(g.getId())
                .anoPeriodo(g.getAnoPeriodo())
                .turmasOferecidasIds(g.getTurmasOferecidas() == null ? null : g.getTurmasOferecidas().stream().map(t -> t.getId()).collect(Collectors.toSet()))
                .cursoId(g.getCurso() == null ? null : g.getCurso().getId())
                .build();
    }

}
