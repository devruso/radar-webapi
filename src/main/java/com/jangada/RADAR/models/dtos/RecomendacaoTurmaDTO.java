package com.jangada.RADAR.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecomendacaoTurmaDTO {

    private TurmaDTO turma;
    private String dificuldade; // FACIL, INTERMEDIO, DIFICIL
    private Double scoreProfessor; // Score médio do professor (1-5, onde 5 é melhor)
    private String motivo; // Breve explicação da recomendação
    private Integer posicao; // Posição na lista ordenada
}
