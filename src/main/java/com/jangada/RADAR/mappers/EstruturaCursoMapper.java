package com.jangada.RADAR.mappers;

import com.jangada.RADAR.models.dtos.EstruturaCursoDTO;
import com.jangada.RADAR.models.entities.EstruturaCurso;

import java.util.stream.Collectors;

public class EstruturaCursoMapper {

    public static EstruturaCursoDTO toDto(EstruturaCurso e) {
        if (e == null) return null;
        return EstruturaCursoDTO.builder()
                .id(e.getId())
                .curso(e.getCurso())
                .municipio(e.getMunicipio())
                .entrada(e.getEntrada())
                .codigo(e.getCodigo())
                .chOptativa(e.getChOptativa())
                .chObrigatoria(e.getChObrigatoria())
                .chComplementar(e.getChComplementar())
                .componentesObrigatoriosIds(e.getComponentesObrigatorios() == null ? null : e.getComponentesObrigatorios().stream().map(c -> c.getId()).collect(Collectors.toSet()))
                .build();
    }

}
