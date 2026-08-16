package com.jangada.RADAR.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.jangada.RADAR.models.entities.ComponenteCurricularContexto;
import com.jangada.RADAR.models.entities.PreRequisito;
import com.jangada.RADAR.models.entities.Turma;
import com.jangada.RADAR.models.entities.Usuario;
import com.jangada.RADAR.repositories.AvaliacaoProfessorRepository;
import com.jangada.RADAR.repositories.ComponenteCurricularRepository;
import com.jangada.RADAR.repositories.ComponenteCurricularContextoRepository;
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
    private final ComponenteCurricularContextoRepository contextoRepository;
    private final AvaliacaoProfessorRepository avaliacaoRepository;
    private final PreRequisitoRepository preRequisiteRepository;

    public RecomendacaoServiceImpl(
        UsuarioRepository usuarioRepository,
        TurmaRepository turmaRepository,
        ComponenteCurricularRepository componenteRepository,
        ComponenteCurricularContextoRepository contextoRepository,
        AvaliacaoProfessorRepository avaliacaoRepository,
        PreRequisitoRepository preRequisiteRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.turmaRepository = turmaRepository;
        this.componenteRepository = componenteRepository;
        this.contextoRepository = contextoRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.preRequisiteRepository = preRequisiteRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecomendacaoTurmaDTO> recomendar(Long usuarioId, String metodo) {
        Usuario usuario = usuarioRepository.findByIdWithDetails(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        if (usuario.getCurso() == null || usuario.getCurso().getNome() == null) {
            throw new IllegalArgumentException("Usuário não possui curso configurado");
        }
        List<ComponenteCurricularContexto> courseContexts = contextoRepository
            .findAllActiveByCourseName(usuario.getCurso().getNome()).stream()
            // A mandatory component cannot receive an official priority when
            // its curricular semester is absent. Keep it out instead of guessing.
            .filter(context -> !context.isRequired()
                || context.getRecommendedPeriod() != null
                    && context.getRecommendedPeriod() > 0)
            .toList();
        Map<Long, ComponenteCurricularContexto> contextsByComponent = courseContexts.stream()
            .collect(Collectors.toMap(
                context -> context.getComponenteCurricular().getId(),
                context -> context,
                RecomendacaoServiceImpl::newerContext
            ));
        Set<Long> directComponentIds = contextsByComponent.keySet();
        Set<String> equivalentCodes = contextsByComponent.values().stream()
            .map(ComponenteCurricularContexto::getComponenteCurricular)
            .map(ComponenteCurricular::getEquivalencias)
            .flatMap(raw -> RecomendacaoUtil.extrairCodigosComponente(raw).stream())
            .collect(Collectors.toSet());

        // Components discovered only in a SIGAA offering remain visible in the
        // catalog, but enter recommendations only with curricular or explicit
        // equivalence evidence.
        List<Turma> allClasses = turmaRepository.findAllActiveWithDetails().stream()
            .filter(turma -> directComponentIds.contains(turma.getComponenteCurricular().getId())
                || equivalentCodes.contains(normalizeCode(
                    turma.getComponenteCurricular().getCodigo()
                )))
            .toList();
        Set<Long> equivalentComponentIds = allClasses.stream()
            .map(Turma::getComponenteCurricular)
            .filter(component -> !directComponentIds.contains(component.getId()))
            .filter(component -> equivalentCodes.contains(normalizeCode(component.getCodigo())))
            .map(ComponenteCurricular::getId)
            .collect(Collectors.toSet());

        Map<Long, Set<String>> requirements = new HashMap<>();
        allClasses.stream()
            .map(Turma::getComponenteCurricular)
            .filter(component -> component != null && component.getId() != null)
            .forEach(component -> requirements
                .computeIfAbsent(component.getId(), ignored -> new HashSet<>())
                .addAll(RecomendacaoUtil.extrairCodigosPreRequisito(component.getPrerequisito())));
        for (PreRequisito prerequisite : preRequisiteRepository.findAllWithComponents()) {
            if (!"PREREQUISITO".equalsIgnoreCase(prerequisite.getTipo())) {
                continue;
            }
            requirements.computeIfAbsent(
                prerequisite.getComponente().getId(), ignored -> new HashSet<>()
            ).add(prerequisite.getComponentePreRequisito().getCodigo().trim().toUpperCase());
        }

        List<Turma> filteredClasses = RecomendacaoUtil.filtrarTurmas(usuario, allClasses, requirements);
        List<Long> componentIds = filteredClasses.stream()
            .map(Turma::getComponenteCurricular)
            .map(ComponenteCurricular::getId)
            .distinct()
            .toList();
        Map<String, Double> scores = componentIds.isEmpty()
            ? Map.of()
            : avaliacaoRepository.findAverageScoresByComponentIds(componentIds).stream()
                .collect(Collectors.toMap(
                    score -> RecomendacaoUtil.scoreKey(score.getProfessorNome(), score.getComponenteId()),
                    score -> score.getScore() == null ? 3.0 : score.getScore()
                ));

        List<RecomendacaoUtil.RecomendacaoCriteria> ranked =
            RecomendacaoUtil.ordenarPorPrioridadeSigaa(
                usuario,
                filteredClasses,
                scores,
                contextsByComponent,
                equivalentComponentIds
            );
        int maximum = usuario.getLimiteMatricula() == null || usuario.getLimiteMatricula() <= 0
            ? 8
            : Math.min(usuario.getLimiteMatricula(), 8);
        List<RecomendacaoUtil.RecomendacaoCriteria> scheduled =
            RecomendacaoUtil.encaixarTurmas(ranked, 3, maximum, metodo);

        List<RecomendacaoTurmaDTO> result = new ArrayList<>(scheduled.size());
        for (int index = 0; index < scheduled.size(); index++) {
            RecomendacaoUtil.RecomendacaoCriteria criteria = scheduled.get(index);
            result.add(RecomendacaoTurmaDTO.builder()
                .turma(TurmaMapper.toDto(criteria.turma))
                .dificuldade(criteria.dificuldade)
                .scoreProfessor(criteria.scoreProfessor)
                .motivo(criteria.motivo)
                .posicao(index + 1)
                .prioridadeMatricula(criteria.prioridadeMatricula.getOrdem())
                .categoriaPrioridade(criteria.prioridadeMatricula.getDescricao())
                .semestreCurricular(criteria.semestreCurricular)
                .semestreAcademico(criteria.semestreAcademico)
                .criterioDesempate("CR entre estudantes na mesma prioridade; score do professor apenas no RADAR")
                .build());
        }
        return result;
    }

    private static ComponenteCurricularContexto newerContext(
        ComponenteCurricularContexto first,
        ComponenteCurricularContexto second
    ) {
        return Comparator.comparing(
                ComponenteCurricularContexto::getImplementationSemester,
                Comparator.nullsFirst(Comparator.naturalOrder())
            )
            .compare(first, second) >= 0 ? first : second;
    }

    private static String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(java.util.Locale.ROOT);
    }

    @Override
    public AvaliacaoProfessorDTO avaliarProfessor(
        Long usuarioId,
        String professorNome,
        Long componenteId,
        Integer nota,
        String comentario
    ) {
        if (nota == null || nota < 1 || nota > 5) {
            throw new IllegalArgumentException("Nota deve estar entre 1 e 5");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        ComponenteCurricular componente = componenteRepository.findById(componenteId)
            .orElseThrow(() -> new ResourceNotFoundException("Componente não encontrado"));

        AvaliacaoProfessor avaliacao = avaliacaoRepository
            .findByUsuarioIdAndProfessorNomeAndComponenteId(usuarioId, professorNome, componenteId)
            .orElseGet(() -> AvaliacaoProfessor.builder()
                .usuario(usuario)
                .professorNome(professorNome)
                .componente(componente)
                .build());
        avaliacao.setNota(nota);
        avaliacao.setComentario(comentario);
        avaliacao.setDataAvaliacao(LocalDateTime.now());
        return AvaliacaoProfessorMapper.toDto(avaliacaoRepository.save(avaliacao));
    }

    @Override
    @Transactional(readOnly = true)
    public Double obterScoreProfessor(String professorNome, Long componenteId) {
        return RecomendacaoUtil.calcularScoreProfessor(professorNome, componenteId, avaliacaoRepository);
    }
}
