package com.jangada.RADAR.mappers;

import java.util.stream.Collectors;

import com.jangada.RADAR.models.dtos.UsuarioDTO;
import com.jangada.RADAR.models.entities.Usuario;

public class UsuarioMapper {

    public static UsuarioDTO toDto(Usuario u) {
        if (u == null) return null;
        return UsuarioDTO.builder()
                .id(u.getId())
                .nome(u.getNome())
                .matricula(u.getMatricula())
                .email(u.getEmail())
                .limiteMatricula(u.getLimiteMatricula())
                .tempoEstudo(u.getTempoEstudo())
                .tempoTransporte(u.getTempoTransporte())
                .anoIngresso(u.getAnoIngresso())
                .mesIngresso(u.getMesIngresso())
                .periodoAtual(u.getPeriodoAtual())
                .isTeste(u.getIsTeste())
                .turnosLivres(u.getTurnosLivres())
                .professoresExcluidos(u.getProfessoresExcluidos())
                .disciplinasFeitas(u.getDisciplinasFeitas())
                .turmasSelecionadasIds(u.getTurmasSelecionadas() == null ? null : u.getTurmasSelecionadas().stream().map(t -> t.getId()).collect(Collectors.toSet()))
                .cursoId(u.getCurso() == null ? null : u.getCurso().getId())
                .build();
    }

    public static Usuario toEntity(UsuarioDTO dto) {
        if (dto == null) return null;
        Usuario u = new Usuario();
        u.setId(dto.getId());
        u.setNome(dto.getNome());
        u.setMatricula(dto.getMatricula());
        u.setEmail(dto.getEmail());
        u.setLimiteMatricula(dto.getLimiteMatricula());
        u.setTempoEstudo(dto.getTempoEstudo());
        u.setTempoTransporte(dto.getTempoTransporte());
        u.setAnoIngresso(dto.getAnoIngresso());
        u.setMesIngresso(dto.getMesIngresso());
        u.setPeriodoAtual(dto.getPeriodoAtual());
        u.setIsTeste(dto.getIsTeste());
        u.setTurnosLivres(dto.getTurnosLivres());
        u.setProfessoresExcluidos(dto.getProfessoresExcluidos());
        u.setDisciplinasFeitas(dto.getDisciplinasFeitas());
        // associations (turmas, curso) must be set at service layer
        return u;
    }
}
