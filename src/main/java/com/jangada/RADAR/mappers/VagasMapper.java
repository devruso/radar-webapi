package com.jangada.RADAR.mappers;

import com.jangada.RADAR.models.dtos.VagasDTO;
import com.jangada.RADAR.models.entities.Vagas;

public class VagasMapper {

    public static VagasDTO toDto(Vagas v) {
        if (v == null) return null;
        return VagasDTO.builder()
                .id(v.getId())
                .totalVagas(v.getTotalVagas())
                .reservaVagas(v.getReservaVagas())
                .build();
    }

}
