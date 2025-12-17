package com.jangada.RADAR.mappers;

import com.jangada.RADAR.models.dtos.CursoDTO;
import com.jangada.RADAR.models.entities.Curso;

public class CursoMapper {

    public static CursoDTO toDto(Curso c) {
        if (c == null) return null;
        return CursoDTO.builder()
                .id(c.getId())
                .nome(c.getNome())
                .coordenador(c.getCoordenador())
                .nivel(c.getNivel())
                .turno(c.getTurno())
                .estruturaId(c.getEstruturaCurso() == null ? null : c.getEstruturaCurso().getId())
                .guiaId(c.getGuiaMatricula() == null ? null : c.getGuiaMatricula().getId())
                .build();
    }

}
