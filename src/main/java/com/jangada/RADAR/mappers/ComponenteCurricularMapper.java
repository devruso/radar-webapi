package com.jangada.RADAR.mappers;

import com.jangada.RADAR.models.dtos.ComponenteCurricularDTO;
import com.jangada.RADAR.models.entities.ComponenteCurricular;

import java.util.stream.Collectors;

public class ComponenteCurricularMapper {

    public static ComponenteCurricularDTO toDto(ComponenteCurricular c) {
        if (c == null) return null;
        return ComponenteCurricularDTO.builder()
                .id(c.getId())
                .codigo(c.getCodigo())
                .nome(c.getNome())
                .nivel(c.getNivel())
                .ementa(c.getEmenta())
                .tipo(c.getTipo())
                .prerequisito(c.getPrerequisito())
                .corequisito(c.getCorequisito())
                .posrequisito(c.getPosrequisito())
                .turmasIds(c.getTurmas() == null ? null : c.getTurmas().stream().map(t -> t.getId()).collect(Collectors.toSet()))
                .build();
    }

}
