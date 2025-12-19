package com.jangada.RADAR.models.entities;

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

import java.time.LocalDateTime;

@Entity
@Table(
    name = "avaliacoes_professor",
    uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "professor_nome", "componente_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvaliacaoProfessor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    private String professorNome;

    @ManyToOne
    @JoinColumn(name = "componente_id", nullable = false)
    private ComponenteCurricular componente;

    // Escala: 1-5 (1=ruim, 5=excelente)
    private Integer nota;

    private String comentario;

    private LocalDateTime dataAvaliacao;
}
