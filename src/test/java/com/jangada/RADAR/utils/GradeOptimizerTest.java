package com.jangada.RADAR.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.jangada.RADAR.models.entities.ComponenteCurricular;
import com.jangada.RADAR.models.entities.Horario;
import com.jangada.RADAR.models.entities.Turma;
import com.jangada.RADAR.models.entities.Vagas;
import com.jangada.RADAR.models.enums.PrioridadeMatricula;
import com.jangada.RADAR.utils.RecomendacaoUtil.RecomendacaoCriteria;

class GradeOptimizerTest {

    @Test
    void greedyNeverReturnsOverlappingClassesOrDuplicateComponents() {
        RecomendacaoCriteria first = candidate(1L, 10L, "MAT001", "SEG", "08:00-10:00");
        RecomendacaoCriteria overlap = candidate(2L, 11L, "MAT002", "SEG", "09:00-11:00");
        RecomendacaoCriteria sameComponent = candidate(3L, 10L, "MAT001", "TER", "08:00-10:00");
        RecomendacaoCriteria compatible = candidate(4L, 12L, "MAT003", "SEG", "10:00-12:00");

        List<RecomendacaoCriteria> result = GradeOptimizer.optimize(
            List.of(first, overlap, sameComponent, compatible), 8, "guloso"
        );

        assertThat(result).extracting(item -> item.turma.getId()).containsExactly(1L, 4L);
    }

    @Test
    void searchCanPreferTwoCompatibleClassesOverOneTopRankedLongClass() {
        RecomendacaoCriteria longClass = candidate(1L, 10L, "MAT001", "SEG", "08:00-12:00");
        RecomendacaoCriteria early = candidate(2L, 11L, "MAT002", "SEG", "08:00-10:00");
        RecomendacaoCriteria late = candidate(3L, 12L, "MAT003", "SEG", "10:00-12:00");

        List<RecomendacaoCriteria> greedy = GradeOptimizer.optimize(
            List.of(longClass, early, late), 2, "guloso"
        );
        List<RecomendacaoCriteria> search = GradeOptimizer.optimize(
            List.of(longClass, early, late), 2, "busca"
        );

        assertThat(greedy).extracting(item -> item.turma.getId()).containsExactly(1L);
        assertThat(search).extracting(item -> item.turma.getId()).containsExactly(2L, 3L);
    }

    @Test
    void classWithoutACompleteScheduleIsNotPresentedAsConflictFree() {
        RecomendacaoCriteria unknown = candidate(1L, 10L, "MAT001", null, null);

        assertThat(GradeOptimizer.optimize(List.of(unknown), 8, "busca")).isEmpty();
    }

    @Test
    void searchDoesNotTradeOneHigherPriorityClassForLowerPriorityClasses() {
        RecomendacaoCriteria priorityOne = candidate(1L, 10L, "MAT001", "SEG", "08:00-12:00");
        priorityOne.prioridadeMatricula = PrioridadeMatricula.I;
        RecomendacaoCriteria priorityThreeEarly = candidate(2L, 11L, "MAT002", "SEG", "08:00-10:00");
        RecomendacaoCriteria priorityThreeLate = candidate(3L, 12L, "MAT003", "SEG", "10:00-12:00");

        List<RecomendacaoCriteria> result = GradeOptimizer.optimize(
            List.of(priorityOne, priorityThreeEarly, priorityThreeLate), 2, "busca"
        );

        assertThat(result).extracting(item -> item.turma.getId()).containsExactly(1L);
    }

    private static RecomendacaoCriteria candidate(
        Long classId,
        Long componentId,
        String code,
        String day,
        String interval
    ) {
        ComponenteCurricular component = ComponenteCurricular.builder()
            .id(componentId)
            .codigo(code)
            .nome(code)
            .nivel((short) 1)
            .build();
        Map<String, String> slots = new LinkedHashMap<>();
        if (day != null) {
            slots.put(day, interval);
        }
        Horario schedule = Horario.builder()
            .id(classId)
            .turno("Matutino")
            .horarios(slots)
            .build();
        Vagas vacancies = Vagas.builder().id(classId).totalVagas((short) 10).build();
        Turma turma = Turma.builder()
            .id(classId)
            .numero("T" + classId)
            .professor("Professor " + classId)
            .componenteCurricular(component)
            .horario(schedule)
            .vagas(vacancies)
            .build();
        return new RecomendacaoCriteria(turma, "FACIL", 3.0, "test");
    }
}
