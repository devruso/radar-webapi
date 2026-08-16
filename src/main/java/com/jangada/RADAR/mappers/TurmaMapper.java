package com.jangada.RADAR.mappers;

import com.jangada.RADAR.models.dtos.TurmaDTO;
import com.jangada.RADAR.models.entities.Turma;

public class TurmaMapper {

    public static TurmaDTO toDto(Turma t) {
        if (t == null) return null;
        return TurmaDTO.builder()
                .id(t.getId())
                .local(t.getLocal())
                .professor(t.getProfessor())
                .numero(t.getNumero())
                .tipo(t.getTipo())
                .componenteId(t.getComponenteCurricular() == null ? null : t.getComponenteCurricular().getId())
                .horarioId(t.getHorario() == null ? null : t.getHorario().getId())
                .vagasId(t.getVagas() == null ? null : t.getVagas().getId())
                .guiaId(t.getGuiaMatricula() == null ? null : t.getGuiaMatricula().getId())
                .componenteCodigo(t.getComponenteCurricular() == null ? null : t.getComponenteCurricular().getCodigo())
                .componenteNome(t.getComponenteCurricular() == null ? null : t.getComponenteCurricular().getNome())
                .turno(t.getHorario() == null ? null : t.getHorario().getTurno())
                .horarios(t.getHorario() == null ? null : t.getHorario().getHorarios())
                .totalVagas(t.getVagas() == null ? null : t.getVagas().getTotalVagas())
                .vagasDisponiveis(t.getVagas() == null ? null : t.getVagas().getVagasDisponiveis())
                .periodoLetivo(t.getPeriodoLetivo())
                .source(t.getSource())
                .externalKey(t.getExternalKey())
                .ativa(t.isAtiva())
                .build();
    }

    public static Turma toEntity(TurmaDTO dto) {
        if (dto == null) return null;
        Turma t = new Turma();
        t.setId(dto.getId());
        t.setLocal(dto.getLocal());
        t.setProfessor(dto.getProfessor());
        t.setNumero(dto.getNumero());
        t.setTipo(dto.getTipo());
        t.setPeriodoLetivo(dto.getPeriodoLetivo());
        t.setSource(dto.getSource());
        t.setExternalKey(dto.getExternalKey());
        t.setAtiva(dto.isAtiva());
        // associations must be set at service layer or by client
        return t;
    }

}
