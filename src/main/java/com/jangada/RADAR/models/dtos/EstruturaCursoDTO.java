package com.jangada.RADAR.models.dtos;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstruturaCursoDTO {
    private Long id;
    private String curso;
    private String municipio;
    private String entrada;
    private String codigo;

    private Integer chOptativa;
    private Integer chObrigatoria;
    private Integer chComplementar;

    private Set<Long> componentesObrigatoriosIds;
}
