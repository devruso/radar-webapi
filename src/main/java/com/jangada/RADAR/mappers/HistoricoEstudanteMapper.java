package com.jangada.RADAR.mappers;

import com.jangada.RADAR.models.dtos.HistoricoEstudanteDTO;
import com.jangada.RADAR.models.entities.HistoricoEstudante;

public class HistoricoEstudanteMapper {
    
    public static HistoricoEstudanteDTO toDto(HistoricoEstudante entity) {
        if (entity == null) {
            return null;
        }
        
        return HistoricoEstudanteDTO.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuario() != null ? entity.getUsuario().getId() : null)
                .componenteId(entity.getComponente() != null ? entity.getComponente().getId() : null)
                .componenteNome(entity.getComponente() != null ? entity.getComponente().getNome() : null)
                .componenteCodigo(entity.getComponente() != null ? entity.getComponente().getCodigo() : null)
                .semestre(entity.getSemestre())
                .nota(entity.getNota())
                .status(entity.getStatus())
                .dataConclusao(entity.getDataConclusao())
                .build();
    }

}
