package com.jangada.RADAR.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.jangada.RADAR.models.entities.AvaliacaoProfessor;
import com.jangada.RADAR.models.entities.ComponenteCurricular;
import com.jangada.RADAR.models.entities.ComponenteCurricularContexto;
import com.jangada.RADAR.models.entities.PreRequisito;
import com.jangada.RADAR.models.entities.Turma;
import com.jangada.RADAR.models.entities.Usuario;
import com.jangada.RADAR.models.enums.PrioridadeMatricula;
import com.jangada.RADAR.repositories.AvaliacaoProfessorRepository;
import com.jangada.RADAR.repositories.PreRequisitoRepository;

public final class RecomendacaoUtil {

    private static final Pattern COMPONENT_CODE = Pattern.compile("\\b[A-Z]{2,5}\\d{2,4}\\b");

    private RecomendacaoUtil() {
    }

    public static class RecomendacaoCriteria {
        public String dificuldade;
        public Double scoreProfessor;
        public String motivo;
        public Turma turma;
        public PrioridadeMatricula prioridadeMatricula;
        public Integer semestreCurricular;
        public Integer semestreAcademico;

        public RecomendacaoCriteria(Turma turma, String dificuldade, Double scoreProfessor, String motivo) {
            this(turma, dificuldade, scoreProfessor, motivo, PrioridadeMatricula.III, null, null);
        }

        public RecomendacaoCriteria(
            Turma turma,
            String dificuldade,
            Double scoreProfessor,
            String motivo,
            PrioridadeMatricula prioridadeMatricula,
            Integer semestreCurricular,
            Integer semestreAcademico
        ) {
            this.turma = turma;
            this.dificuldade = dificuldade;
            this.scoreProfessor = scoreProfessor;
            this.motivo = motivo;
            this.prioridadeMatricula = prioridadeMatricula;
            this.semestreCurricular = semestreCurricular;
            this.semestreAcademico = semestreAcademico;
        }
    }

    /** Compatibility overload retained for callers outside the request path. */
    public static List<Turma> filtrarTurmas(
        Usuario usuario,
        List<Turma> turmasDisponiveis,
        AvaliacaoProfessorRepository avaliacaoRepo,
        PreRequisitoRepository preReqRepo
    ) {
        Map<Long, Set<String>> requirements = turmasDisponiveis.stream()
            .filter(turma -> turma.getComponenteCurricular() != null)
            .map(Turma::getComponenteCurricular)
            .distinct()
            .collect(Collectors.toMap(
                ComponenteCurricular::getId,
                component -> preReqRepo.findByComponenteIdAndTipo(component.getId(), "PREREQUISITO")
                    .stream()
                    .map(PreRequisito::getComponentePreRequisito)
                    .map(ComponenteCurricular::getCodigo)
                    .map(RecomendacaoUtil::normalize)
                    .collect(Collectors.toSet())
            ));
        return filtrarTurmas(usuario, turmasDisponiveis, requirements);
    }

    /** Pure filtering variant; all related data is loaded in bulk by the service. */
    public static List<Turma> filtrarTurmas(
        Usuario usuario,
        List<Turma> turmasDisponiveis,
        Map<Long, Set<String>> requisitosPorComponente
    ) {
        Set<String> completed = normalizeSet(usuario.getDisciplinasFeitas());
        Set<String> bannedProfessors = normalizeSet(usuario.getProfessoresExcluidos());

        return turmasDisponiveis.stream()
            .filter(turma -> turma.getComponenteCurricular() != null)
            .filter(turma -> !completed.contains(normalize(turma.getComponenteCurricular().getCodigo())))
            .filter(turma -> !bannedProfessors.contains(normalize(turma.getProfessor())))
            // Public SIGAA listings do not always expose vacancy counts. An
            // unknown value remains eligible; an explicitly exhausted class does not.
            .filter(turma -> turma.getVagas() != null)
            .filter(turma -> turma.getVagas().getVagasDisponiveis() == null
                || turma.getVagas().getVagasDisponiveis() > 0)
            .filter(turma -> turma.getVagas().getTotalVagas() == null
                || turma.getVagas().getTotalVagas() > 0)
            .filter(turma -> requisitosPorComponente
                .getOrDefault(turma.getComponenteCurricular().getId(), Set.of())
                .stream()
                .allMatch(completed::contains))
            .filter(turma -> verificarTurnoDisponivel(turma, usuario.getTurnosLivres()))
            .toList();
    }

    public static Set<String> extrairCodigosPreRequisito(String rawPrerequisites) {
        return extrairCodigosComponente(rawPrerequisites);
    }

    public static Set<String> extrairCodigosComponente(String rawCodes) {
        if (rawCodes == null || rawCodes.isBlank()) {
            return Set.of();
        }
        Set<String> codes = new LinkedHashSet<>();
        Matcher matcher = COMPONENT_CODE.matcher(normalize(rawCodes));
        while (matcher.find()) {
            codes.add(matcher.group());
        }
        return Set.copyOf(codes);
    }

    public static boolean verificarPreRequisitos(
        ComponenteCurricular componente,
        Set<String> disciplinasFeitas,
        PreRequisitoRepository preReqRepo
    ) {
        if (componente == null) {
            return false;
        }
        Set<String> completed = normalizeSet(disciplinasFeitas);
        return preReqRepo.findByComponenteIdAndTipo(componente.getId(), "PREREQUISITO")
            .stream()
            .map(PreRequisito::getComponentePreRequisito)
            .map(ComponenteCurricular::getCodigo)
            .map(RecomendacaoUtil::normalize)
            .allMatch(completed::contains);
    }

    public static boolean verificarTurnoDisponivel(Turma turma, List<Boolean> turnosLivres) {
        if (turnosLivres == null || turnosLivres.isEmpty()) {
            return true;
        }
        if (turma.getHorario() == null) {
            return false;
        }
        Integer index = mapearTurnoParaIndice(turma.getHorario().getTurno());
        return index != null
            && index < turnosLivres.size()
            && Boolean.TRUE.equals(turnosLivres.get(index));
    }

    private static Integer mapearTurnoParaIndice(String turno) {
        String value = normalize(turno);
        if (value.contains("MATUTINO") || value.contains("MANHA")) return 0;
        if (value.contains("VESPERTINO") || value.contains("TARDE")) return 1;
        if (value.contains("NOTURNO") || value.contains("NOITE")) return 2;
        return null;
    }

    public static Double calcularScoreProfessor(
        String professorNome,
        Long componenteId,
        AvaliacaoProfessorRepository avaliacaoRepo
    ) {
        List<AvaliacaoProfessor> reviews = avaliacaoRepo
            .findByProfessorNomeAndComponenteId(professorNome, componenteId);
        return reviews.stream()
            .mapToInt(review -> review.getNota() == null ? 3 : review.getNota())
            .average()
            .orElse(3.0);
    }

    public static String classificarDificuldade(ComponenteCurricular componente) {
        if (componente == null || componente.getNivel() == null) return "INTERMEDIO";
        if (componente.getNivel() <= 2) return "FACIL";
        if (componente.getNivel() >= 5) return "DIFICIL";
        return "INTERMEDIO";
    }

    public static List<RecomendacaoCriteria> ordenarPorPrioridadeSigaa(
        Usuario usuario,
        List<Turma> turmas,
        Map<String, Double> scores,
        Map<Long, ComponenteCurricularContexto> contextosPorComponente,
        Set<Long> componentesEquivalentes
    ) {
        List<RecomendacaoCriteria> recommendations = new ArrayList<>();
        for (Turma turma : turmas) {
            ComponenteCurricular component = turma.getComponenteCurricular();
            ComponenteCurricularContexto context = contextosPorComponente.get(component.getId());
            boolean equivalent = componentesEquivalentes.contains(component.getId());
            PrioridadeMatricula priority = classificarPrioridade(usuario, context, equivalent);
            String difficulty = classificarDificuldade(component);
            Double score = scores.getOrDefault(scoreKey(turma.getProfessor(), component.getId()), 3.0);
            Short available = turma.getVagas().getVagasDisponiveis();
            String vacancies = available == null
                ? "disponibilidade de vagas não publicada"
                : available + " vaga(s) disponível(is)";
            String reason = String.format(
                "Prioridade %s do SIGAA: %s. CR %s é usado pelo SIGAA apenas para "
                    + "desempatar estudantes na mesma prioridade; no RADAR, score do professor %.1f; %s",
                priority.name(),
                priority.getDescricao(),
                usuario.getCoeficienteRendimento() == null
                    ? "não informado"
                    : usuario.getCoeficienteRendimento().toPlainString(),
                score,
                vacancies
            );
            recommendations.add(new RecomendacaoCriteria(
                turma,
                difficulty,
                score,
                reason,
                priority,
                context == null ? null : context.getRecommendedPeriod(),
                usuario.getPeriodoAtual()
            ));
        }
        recommendations.sort(Comparator
            .comparingInt((RecomendacaoCriteria item) -> item.prioridadeMatricula.getOrdem())
            .thenComparing((RecomendacaoCriteria item) -> item.scoreProfessor, Comparator.reverseOrder())
            .thenComparing(item -> normalize(item.turma.getComponenteCurricular().getCodigo()))
            .thenComparing(item -> item.turma.getId(), Comparator.nullsLast(Comparator.naturalOrder())));
        return recommendations;
    }

    public static PrioridadeMatricula classificarPrioridade(
        Usuario usuario,
        ComponenteCurricularContexto context,
        boolean equivalent
    ) {
        if (usuario == null || usuario.getPeriodoAtual() == null || usuario.getPeriodoAtual() < 1) {
            throw new IllegalArgumentException("Semestre acadêmico do estudante não foi configurado");
        }
        if (!equivalent && context != null && context.isRequired()
            && context.getRecommendedPeriod() != null
            && context.getRecommendedPeriod().equals(usuario.getPeriodoAtual())) {
            return PrioridadeMatricula.I;
        }
        if (Boolean.TRUE.equals(usuario.getStatusFormando())) {
            return PrioridadeMatricula.II;
        }
        if (equivalent || context == null) {
            return PrioridadeMatricula.V;
        }
        if (!context.isRequired()) {
            return PrioridadeMatricula.III;
        }
        if (context.getRecommendedPeriod() == null || context.getRecommendedPeriod() < 1) {
            throw new IllegalArgumentException(
                "Componente obrigatório sem semestre curricular válido: "
                    + context.getComponenteCurricular().getCodigo()
            );
        }
        return context.getRecommendedPeriod() < usuario.getPeriodoAtual()
            ? PrioridadeMatricula.III
            : PrioridadeMatricula.IV;
    }

    public static String scoreKey(String professorNome, Long componenteId) {
        return normalize(professorNome) + "|" + componenteId;
    }

    public static List<RecomendacaoCriteria> encaixarTurmas(
        List<RecomendacaoCriteria> recomendacoes,
        int minimo,
        int maximo
    ) {
        return encaixarTurmas(recomendacoes, minimo, maximo, "guloso");
    }

    public static List<RecomendacaoCriteria> encaixarTurmas(
        List<RecomendacaoCriteria> recomendacoes,
        int minimo,
        int maximo,
        String metodo
    ) {
        return GradeOptimizer.optimize(recomendacoes, maximo, metodo);
    }

    private static Set<String> normalizeSet(Set<String> values) {
        if (values == null) return Set.of();
        return values.stream()
            .filter(value -> value != null && !value.isBlank())
            .map(RecomendacaoUtil::normalize)
            .collect(Collectors.toSet());
    }

    private static String normalize(String value) {
        if (value == null) return "";
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .trim()
            .toUpperCase(Locale.ROOT);
    }
}
