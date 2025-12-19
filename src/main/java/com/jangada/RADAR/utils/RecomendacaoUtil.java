package com.jangada.RADAR.utils;

import com.jangada.RADAR.models.entities.AvaliacaoProfessor;
import com.jangada.RADAR.models.entities.ComponenteCurricular;
import com.jangada.RADAR.models.entities.PreRequisito;
import com.jangada.RADAR.models.entities.Turma;
import com.jangada.RADAR.models.entities.Usuario;
import com.jangada.RADAR.repositories.AvaliacaoProfessorRepository;
import com.jangada.RADAR.repositories.PreRequisitoRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utilitário para lógica de recomendação de disciplinas.
 * 
 * Algoritmo "dumb" (simples):
 * 1. FILTRAR: Remover disciplinas já feitas, com professores excluídos, sem vagas
 * 2. FILTRAR: Verificar pré-requisitos (o aluno tem as disciplinas anteriores?)
 * 3. ORDENAR: Por dificuldade (fácil -> intermediário -> difícil)
 * 4. ENCAIXAR: Retornar no máximo 6-8 disciplinas (min 3) que cabem na grade
 */
public class RecomendacaoUtil {

    public static class RecomendacaoCriteria {
        public String componente;
        public String dificuldade; // FACIL, INTERMEDIO, DIFICIL
        public Double scoreProfessor;
        public String motivo;
        public Turma turma;

        public RecomendacaoCriteria(Turma turma, String dificuldade, Double scoreProfessor, String motivo) {
            this.turma = turma;
            this.dificuldade = dificuldade;
            this.scoreProfessor = scoreProfessor;
            this.motivo = motivo;
        }
    }

    /**
     * Filtra turmas que podem ser recomendadas para o usuário.
     * Remove: já feitas, professor excluído, sem vagas, pré-requisitos não atendidos
     */
    public static List<Turma> filtrarTurmas(
            Usuario usuario,
            List<Turma> turmasDisponiveis,
            AvaliacaoProfessorRepository avaliacaoRepo,
            PreRequisitoRepository preReqRepo) {

        return turmasDisponiveis.stream()
                .filter(turma -> {
                    ComponenteCurricular comp = turma.getComponenteCurricular();

                    // 1. Já fez a disciplina?
                    if (usuario.getDisciplinasFeitas().contains(comp.getCodigo())) {
                        return false;
                    }

                    // 2. Professor excluído?
                    if (usuario.getProfessoresExcluidos().contains(turma.getProfessor())) {
                        return false;
                    }

                    // 3. Tem vagas?
                    if (turma.getVagas() == null || turma.getVagas().getTotalVagas() <= 0) {
                        return false;
                    }

                    // 4. Pré-requisitos atendidos?
                    if (!verificarPreRequisitos(comp, usuario.getDisciplinasFeitas(), preReqRepo)) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * Verifica se o aluno atende aos pré-requisitos de uma disciplina
     */
    public static boolean verificarPreRequisitos(
            ComponenteCurricular componente,
            Set<String> disciplinasFeitas,
            PreRequisitoRepository preReqRepo) {

        List<PreRequisito> preReqs = preReqRepo.findByComponenteIdAndTipo(componente.getId(), "PREREQUISITO");

        for (PreRequisito preReq : preReqs) {
            String codigoPreReq = preReq.getComponentePreRequisito().getCodigo();
            if (!disciplinasFeitas.contains(codigoPreReq)) {
                return false; // Falta algum pré-requisito
            }
        }

        return true;
    }

    /**
     * Calcula o score do professor baseado em avaliações dos alunos.
     * Retorna valor entre 1 e 5 (5 = melhor).
     * Se não tem avaliações, retorna 3 (neutro).
     */
    public static Double calcularScoreProfessor(
            String professorNome,
            Long componenteId,
            AvaliacaoProfessorRepository avaliacaoRepo) {

        List<AvaliacaoProfessor> avaliacoes = avaliacaoRepo.findByProfessorNomeAndComponenteId(professorNome, componenteId);

        if (avaliacoes.isEmpty()) {
            return 3.0; // Score neutro se sem avaliações
        }

        return avaliacoes.stream()
                .mapToInt(a -> a.getNota() != null ? a.getNota() : 3)
                .average()
                .orElse(3.0);
    }

    /**
     * Classifica a disciplina em dificuldade.
     * FACIL: nível <= 2
     * INTERMEDIO: nível 3-4
     * DIFICIL: nível >= 5
     */
    public static String classificarDificuldade(ComponenteCurricular componente) {
        if (componente.getNivel() == null) {
            return "INTERMEDIO";
        }

        if (componente.getNivel() <= 2) {
            return "FACIL";
        } else if (componente.getNivel() >= 5) {
            return "DIFICIL";
        }
        return "INTERMEDIO";
    }

    /**
     * Ordena turmas por dificuldade (fácil -> intermediário -> difícil)
     * e depois por score do professor (maior score primeiro)
     */
    public static List<RecomendacaoCriteria> ordenarPorEstrategia(
            List<Turma> turmas,
            AvaliacaoProfessorRepository avaliacaoRepo) {

        List<RecomendacaoCriteria> recomendacoes = new ArrayList<>();

        for (Turma turma : turmas) {
            ComponenteCurricular comp = turma.getComponenteCurricular();
            String dificuldade = classificarDificuldade(comp);
            Double scoreProfessor = calcularScoreProfessor(turma.getProfessor(), comp.getId(), avaliacaoRepo);

            String motivo = String.format(
                    "Disciplina %s (dificuldade %s) com prof. %s (score: %.1f)",
                    comp.getNome(),
                    dificuldade,
                    turma.getProfessor(),
                    scoreProfessor
            );

            recomendacoes.add(new RecomendacaoCriteria(turma, dificuldade, scoreProfessor, motivo));
        }

        // Ordena: primeiro por dificuldade (FACIL < INTERMEDIO < DIFICIL)
        // depois por score do professor (maior primeiro)
        recomendacoes.sort((a, b) -> {
            int diffCompare = compararDificuldade(a.dificuldade, b.dificuldade);
            if (diffCompare != 0) {
                return diffCompare;
            }
            return b.scoreProfessor.compareTo(a.scoreProfessor);
        });

        return recomendacoes;
    }

    private static int compararDificuldade(String a, String b) {
        int orderA = getDificuldadeOrder(a);
        int orderB = getDificuldadeOrder(b);
        return Integer.compare(orderA, orderB);
    }

    private static int getDificuldadeOrder(String dificuldade) {
        return switch (dificuldade) {
            case "FACIL" -> 0;
            case "INTERMEDIO" -> 1;
            case "DIFICIL" -> 2;
            default -> 1;
        };
    }

    /**
     * Encaixa as turmas recomendadas respeitando:
     * - Mínimo 3 disciplinas
     * - Máximo 8 disciplinas
     * - Sem conflito de horário (se implementado)
     */
    public static List<RecomendacaoCriteria> encaixarTurmas(
            List<RecomendacaoCriteria> recomendacoes,
            int minimo,
            int maximo) {

        // Por enquanto, retorna entre min e max sem validar conflito de horário
        // TODO: implementar validação de conflito de horário
        int quantidade = Math.min(maximo, Math.max(minimo, recomendacoes.size()));
        return recomendacoes.subList(0, quantidade);
    }
}
