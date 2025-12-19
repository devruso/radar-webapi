package com.jangada.RADAR.models.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String matricula;
    private String email;
    private String senha; // BCrypt hash

    private Integer limiteMatricula;
    private Integer tempoEstudo;
    private Integer tempoTransporte;

    // UFBA: ano de ingresso (ex.: 2025) e mês (ex.: 3 para março) e período atual
    private Integer anoIngresso;
    private Integer mesIngresso;
    private Integer periodoAtual;

    // Flag para diferenciar usuário teste (sem cadastro) de usuário autenticado
    @Column(nullable = false)
    @Builder.Default
    private Boolean isTeste = false;

    @ElementCollection
    @CollectionTable(name = "usuario_turnos_livres", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "livre")
    @Builder.Default
    private List<Boolean> turnosLivres = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "usuario_professores_excluidos", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "professor")
    @Builder.Default
    private Set<String> professoresExcluidos = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "usuario_disciplinas_feitas", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "componente_codigo")
    @Builder.Default
    private Set<String> disciplinasFeitas = new HashSet<>();

        @ManyToMany
        @JoinTable(
            name = "usuario_turmas_selecionadas",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "turma_id")
        )
        @Builder.Default
        private Set<Turma> turmasSelecionadas = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "curso_id")
    private Curso curso;

}
