package com.jangada.RADAR.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jangada.RADAR.exceptions.ResourceNotFoundException;
import com.jangada.RADAR.mappers.AvaliacaoProfessorMapper;
import com.jangada.RADAR.mappers.TurmaMapper;
import com.jangada.RADAR.models.dtos.AvaliacaoProfessorDTO;
import com.jangada.RADAR.models.dtos.RecomendacaoTurmaDTO;
import com.jangada.RADAR.models.entities.AvaliacaoProfessor;
import com.jangada.RADAR.models.entities.ComponenteCurricular;
import com.jangada.RADAR.models.entities.Turma;
import com.jangada.RADAR.models.entities.Usuario;
import com.jangada.RADAR.repositories.AvaliacaoProfessorRepository;
import com.jangada.RADAR.repositories.ComponenteCurricularRepository;
import com.jangada.RADAR.repositories.PreRequisitoRepository;
import com.jangada.RADAR.repositories.TurmaRepository;
import com.jangada.RADAR.repositories.UsuarioRepository;
import com.jangada.RADAR.services.RecomendacaoService;
import com.jangada.RADAR.utils.RecomendacaoUtil;

@Service
@Transactional
public class RecomendacaoServiceImpl implements RecomendacaoService {

    private final UsuarioRepository usuarioRepository;
    private final TurmaRepository turmaRepository;
    private final ComponenteCurricularRepository componenteRepository;
    private final AvaliacaoProfessorRepository avaliacaoRepository;
    private final PreRequisitoRepository preRequisiteRepository;

    public RecomendacaoServiceImpl(
            UsuarioRepository usuarioRepository,
            TurmaRepository turmaRepository,
            ComponenteCurricularRepository componenteRepository,
            AvaliacaoProfessorRepository avaliacaoRepository,
            PreRequisitoRepository preRequisiteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.turmaRepository = turmaRepository;
        this.componenteRepository = componenteRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.preRequisiteRepository = preRequisiteRepository;
    }

    @Override
    public List<RecomendacaoTurmaDTO> recomendar(Long usuarioId, String metodo) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Obter todas as turmas disponíveis
        List<Turma> todasAsTurmas = turmaRepository.findAll();

        // PASSO 1: FILTRAR
        List<Turma> turmasFiltradasFiltradas = RecomendacaoUtil.filtrarTurmas(
                usuario,
                todasAsTurmas,
                avaliacaoRepository,
                preRequisiteRepository
        );

        // PASSO 2: ORDENAR
        List<RecomendacaoUtil.RecomendacaoCriteria> turmasOrdenadas = RecomendacaoUtil.ordenarPorEstrategia(
                turmasFiltradasFiltradas,
                avaliacaoRepository
        );

        // PASSO 3: ENCAIXAR (min 3, max 8)
        List<RecomendacaoUtil.RecomendacaoCriteria> turmasEncaixadas = RecomendacaoUtil.encaixarTurmas(
                turmasOrdenadas,
                3,
                8
        );

        // PASSO 4: CONVERTER PARA DTO com posição
        return turmasEncaixadas.stream()
                .map((criteria) -> RecomendacaoTurmaDTO.builder()
                        .turma(TurmaMapper.toDto(criteria.turma))
                        .dificuldade(criteria.dificuldade)
                        .scoreProfessor(criteria.scoreProfessor)
                        .motivo(criteria.motivo)
                        .posicao(turmasEncaixadas.indexOf(criteria) + 1)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public AvaliacaoProfessorDTO avaliarProfessor(
            Long usuarioId,
            String professorNome,
            Long componenteId,
            Integer nota,
            String comentario) {

        // Validar entrada
        if (nota < 1 || nota > 5) {
            throw new IllegalArgumentException("Nota deve estar entre 1 e 5");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        ComponenteCurricular componente = componenteRepository.findById(componenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Componente não encontrado"));

        // Verificar se já existe avaliação deste usuário para este professor nesta disciplina
        AvaliacaoProfessor avaliacao = avaliacaoRepository
                .findByUsuarioIdAndProfessorNomeAndComponenteId(usuarioId, professorNome, componenteId)
                .orElse(null);

        if (avaliacao == null) {
            avaliacao = AvaliacaoProfessor.builder()
                    .usuario(usuario)
                    .professorNome(professorNome)
                    .componente(componente)
                    .nota(nota)
                    .comentario(comentario)
                    .dataAvaliacao(LocalDateTime.now())
                    .build();
        } else {
            // Atualizar avaliação existente
            avaliacao.setNota(nota);
            avaliacao.setComentario(comentario);
            avaliacao.setDataAvaliacao(LocalDateTime.now());
        }

        AvaliacaoProfessor saved = avaliacaoRepository.save(avaliacao);
        return AvaliacaoProfessorMapper.toDto(saved);
    }

    @Override
    public List<AvaliacaoProfessorDTO> obterAvaliacoesProfessor(String professorNome) {
        return avaliacaoRepository.findByProfessorNome(professorNome)
                .stream()
                .map(AvaliacaoProfessorMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Double obterScoreProfessor(String professorNome, Long componenteId) {
        return RecomendacaoUtil.calcularScoreProfessor(professorNome, componenteId, avaliacaoRepository);
    }
}

