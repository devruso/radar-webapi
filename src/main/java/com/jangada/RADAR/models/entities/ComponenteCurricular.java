package com.jangada.RADAR.models.entities;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "componentes_curriculares")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"turmas"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComponenteCurricular {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String codigo;

    private String nome;

    private Short nivel;

    @Lob
    private String ementa;

    private String tipo;

    // Prerequisitos em formato simples (códigos separados por vírgula)
    private String prerequisito;
    private String corequisito;
    private String posrequisito;

    @OneToMany(mappedBy = "componenteCurricular", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Turma> turmas = new HashSet<>();

}
