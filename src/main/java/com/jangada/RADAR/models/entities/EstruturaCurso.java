package com.jangada.RADAR.models.entities;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "estrutura_curso")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstruturaCurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String curso;
    private String municipio;
    private String entrada;
    private String codigo;

    private Integer chOptativa;
    private Integer chObrigatoria;
    private Integer chComplementar;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "estrutura_id")
    @Builder.Default
    private Set<ComponenteCurricular> componentesObrigatorios = new HashSet<>();

}
