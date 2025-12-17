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
public class ComponenteCurricularDTO {
    private Long id;
    private String codigo;
    private String nome;
    private Short nivel;
    private String ementa;
    private String tipo;
    private String prerequisito;
    private String corequisito;
    private String posrequisito;

    private Set<Long> turmasIds;
}
