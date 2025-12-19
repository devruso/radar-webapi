package com.jangada.RADAR.mappers;

import com.jangada.RADAR.models.dtos.RecomendacaoTurmaDTO;
import com.jangada.RADAR.models.dtos.TurmaDTO;
import com.jangada.RADAR.models.entities.Turma;

public class RecomendacaoTurmaMapper {

    public static RecomendacaoTurmaDTO toDto(
            Turma turma,
            String dificuldade,
            Double scoreProfessor,
            String motivo,
            Integer posicao) {
        if (turma == null) return null;
        return RecomendacaoTurmaDTO.builder()
                .turma(TurmaMapper.toDto(turma))
                .dificuldade(dificuldade)
                .scoreProfessor(scoreProfessor)
                .motivo(motivo)
                .posicao(posicao)
                .build();
    }
}
