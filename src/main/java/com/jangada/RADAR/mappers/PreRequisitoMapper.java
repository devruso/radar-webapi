package com.jangada.RADAR.mappers;

import com.jangada.RADAR.models.dtos.PreRequisitoDTO;
import com.jangada.RADAR.models.entities.PreRequisito;

public class PreRequisitoMapper {

    public static PreRequisitoDTO toDto(PreRequisito p) {
        if (p == null) return null;
        return PreRequisitoDTO.builder()
                .id(p.getId())
                .componenteId(p.getComponente() == null ? null : p.getComponente().getId())
                .componentePreRequisitoId(p.getComponentePreRequisito() == null ? null : p.getComponentePreRequisito().getId())
                .tipo(p.getTipo())
                .build();
    }

    public static PreRequisito toEntity(PreRequisitoDTO dto) {
        if (dto == null) return null;
        return PreRequisito.builder()
                .id(dto.getId())
                .tipo(dto.getTipo())
                .build();
    }
}
