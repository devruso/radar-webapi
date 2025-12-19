package com.jangada.RADAR.mappers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.jangada.RADAR.models.dtos.PreferenciasUsuarioDTO;
import com.jangada.RADAR.models.entities.PreferenciasUsuario;

public class PreferenciasUsuarioMapper {
    
    public static PreferenciasUsuarioDTO toDto(PreferenciasUsuario entity) {
        if (entity == null) {
            return null;
        }
        
        List<String> turnos = entity.getTurnosDisponiveis() != null && !entity.getTurnosDisponiveis().isEmpty()
                ? Arrays.asList(entity.getTurnosDisponiveis().split(","))
                : Collections.emptyList();
        
        List<String> professores = entity.getProfessoresBanidos() != null && !entity.getProfessoresBanidos().isEmpty()
                ? Arrays.asList(entity.getProfessoresBanidos().split(","))
                : Collections.emptyList();
        
        return PreferenciasUsuarioDTO.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuario() != null ? entity.getUsuario().getId() : null)
                .turnosDisponiveis(turnos)
                .professoresBanidos(professores)
                .dataAtualizacao(entity.getDataAtualizacao())
                .build();
    }
    
    public static PreferenciasUsuario toEntity(PreferenciasUsuarioDTO dto) {
        if (dto == null) {
            return null;
        }
        
        String turnosStr = dto.getTurnosDisponiveis() != null 
                ? dto.getTurnosDisponiveis().stream().collect(Collectors.joining(","))
                : "";
        
        String professoresStr = dto.getProfessoresBanidos() != null
                ? dto.getProfessoresBanidos().stream().collect(Collectors.joining(","))
                : "";
        
        return PreferenciasUsuario.builder()
                .id(dto.getId())
                .turnosDisponiveis(turnosStr)
                .professoresBanidos(professoresStr)
                .build();
    }

}
