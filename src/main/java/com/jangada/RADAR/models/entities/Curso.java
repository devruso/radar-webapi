package com.jangada.RADAR.models.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cursos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String coordenador;
    private String nivel;
    private String turno;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "estrutura_id")
    private EstruturaCurso estruturaCurso;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "guia_id")
    private GuiaMatricula guiaMatricula;

}
