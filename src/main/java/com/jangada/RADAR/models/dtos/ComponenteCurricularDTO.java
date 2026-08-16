package com.jangada.RADAR.models.dtos;

import java.time.Instant;
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

    private String departamento;
    private String nivelAcademico;
    private String semestre;
    private String programa;
    private String objetivo;
    private String metodologia;
    private String avaliacaoAprendizagem;
    private String bibliografia;
    private Integer cargaHoraria;
    private String ementasSources;
    private Instant ementasUpdatedAt;
    private Instant ementasSyncedAt;

    private Set<Long> turmasIds;
}
