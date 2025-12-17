package com.jangada.RADAR.mappers;

import com.jangada.RADAR.models.dtos.HorarioDTO;
import com.jangada.RADAR.models.entities.Horario;

public class HorarioMapper {

    public static HorarioDTO toDto(Horario h) {
        if (h == null) return null;
        return HorarioDTO.builder()
                .id(h.getId())
                .codigo(h.getCodigo())
                .turno(h.getTurno())
                .horarios(h.getHorarios())
                .build();
    }

}
