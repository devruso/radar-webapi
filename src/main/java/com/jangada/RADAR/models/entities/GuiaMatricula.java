package com.jangada.RADAR.models.entities;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "guias_matricula")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuiaMatricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String anoPeriodo;

    @OneToMany(mappedBy = "guiaMatricula", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Turma> turmasOferecidas = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "curso_id")
    private Curso curso;

}
