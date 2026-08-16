package com.jangada.RADAR.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.jangada.RADAR.models.entities.ComponenteCurricular;
import com.jangada.RADAR.models.entities.ComponenteCurricularContexto;
import com.jangada.RADAR.models.entities.Usuario;
import com.jangada.RADAR.models.enums.PrioridadeMatricula;

class RecomendacaoPrioridadeTest {

    @Test
    void followsAllFiveSigaaCategoriesInTheirOfficialOrder() {
        Usuario regular = student(4, false);
        Usuario graduating = student(4, true);

        assertThat(RecomendacaoUtil.classificarPrioridade(regular, context(true, 4), false))
            .isEqualTo(PrioridadeMatricula.I);
        assertThat(RecomendacaoUtil.classificarPrioridade(graduating, context(true, 5), false))
            .isEqualTo(PrioridadeMatricula.II);
        assertThat(RecomendacaoUtil.classificarPrioridade(regular, context(true, 3), false))
            .isEqualTo(PrioridadeMatricula.III);
        assertThat(RecomendacaoUtil.classificarPrioridade(regular, context(false, 0), false))
            .isEqualTo(PrioridadeMatricula.III);
        assertThat(RecomendacaoUtil.classificarPrioridade(regular, context(true, 5), false))
            .isEqualTo(PrioridadeMatricula.IV);
        assertThat(RecomendacaoUtil.classificarPrioridade(regular, null, true))
            .isEqualTo(PrioridadeMatricula.V);
    }

    @Test
    void matchingMandatorySemesterRemainsPriorityOneForGraduatingStudent() {
        assertThat(RecomendacaoUtil.classificarPrioridade(
            student(4, true), context(true, 4), false
        )).isEqualTo(PrioridadeMatricula.I);
    }

    private static Usuario student(int academicSemester, boolean graduating) {
        return Usuario.builder()
            .periodoAtual(academicSemester)
            .coeficienteRendimento(new BigDecimal("8.25"))
            .statusFormando(graduating)
            .build();
    }

    private static ComponenteCurricularContexto context(boolean required, int semester) {
        ComponenteCurricular component = ComponenteCurricular.builder()
            .id((long) semester + (required ? 100 : 200))
            .codigo("MAT" + semester + (required ? "01" : "02"))
            .build();
        return ComponenteCurricularContexto.builder()
            .componenteCurricular(component)
            .isRequired(required)
            .recommendedPeriod(semester)
            .build();
    }
}
