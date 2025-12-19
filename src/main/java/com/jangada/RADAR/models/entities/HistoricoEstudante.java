package com.jangada.RADAR.models.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "historico_estudante", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"usuario_id", "componente_id", "semestre"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricoEstudante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "componente_id", nullable = false)
    private ComponenteCurricular componente;

    @Column(nullable = false, length = 10)
    private String semestre; // Ex: "2024.1"

    private Double nota;

    @Column(nullable = false, length = 20)
    private String status; // APROVADO, REPROVADO, TRANCADO

    @Column(name = "data_conclusao")
    private LocalDate dataConclusao;

}
