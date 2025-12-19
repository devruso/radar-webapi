package com.jangada.RADAR.mappers;

import com.jangada.RADAR.models.dtos.AvaliacaoProfessorDTO;
import com.jangada.RADAR.models.entities.AvaliacaoProfessor;

public class AvaliacaoProfessorMapper {

    public static AvaliacaoProfessorDTO toDto(AvaliacaoProfessor a) {
        if (a == null) return null;
        return AvaliacaoProfessorDTO.builder()
                .id(a.getId())
                .usuarioId(a.getUsuario() == null ? null : a.getUsuario().getId())
                .professorNome(a.getProfessorNome())
                .componenteId(a.getComponente() == null ? null : a.getComponente().getId())
                .nota(a.getNota())
                .comentario(a.getComentario())
                .dataAvaliacao(a.getDataAvaliacao())
                .build();
    }

    public static AvaliacaoProfessor toEntity(AvaliacaoProfessorDTO dto) {
        if (dto == null) return null;
        return AvaliacaoProfessor.builder()
                .id(dto.getId())
                .professorNome(dto.getProfessorNome())
                .nota(dto.getNota())
                .comentario(dto.getComentario())
                .dataAvaliacao(dto.getDataAvaliacao())
                .build();
    }
}
