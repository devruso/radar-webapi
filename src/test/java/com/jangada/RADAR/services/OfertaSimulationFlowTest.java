package com.jangada.RADAR.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.jangada.RADAR.models.dtos.ImportacaoOfertasResultadoDTO;
import com.jangada.RADAR.models.dtos.ImportarOfertasDTO;
import com.jangada.RADAR.models.dtos.OfertaTurmaInputDTO;
import com.jangada.RADAR.models.dtos.RecomendacaoTurmaDTO;
import com.jangada.RADAR.models.dtos.SalvarSimulacaoGradeDTO;
import com.jangada.RADAR.models.dtos.SimulacaoGradeDTO;
import com.jangada.RADAR.models.entities.ComponenteCurricular;
import com.jangada.RADAR.models.entities.ComponenteCurricularContexto;
import com.jangada.RADAR.models.entities.Curso;
import com.jangada.RADAR.models.entities.Usuario;
import com.jangada.RADAR.repositories.ComponenteCurricularContextoRepository;
import com.jangada.RADAR.repositories.ComponenteCurricularRepository;
import com.jangada.RADAR.repositories.CursoRepository;
import com.jangada.RADAR.repositories.TurmaRepository;
import com.jangada.RADAR.repositories.UsuarioRepository;

@SpringBootTest(properties = "debug=false")
@Transactional
class OfertaSimulationFlowTest {

    @Autowired
    private OfertaTurmaService ofertaService;

    @Autowired
    private RecomendacaoService recomendacaoService;

    @Autowired
    private SimulacaoGradeService simulacaoService;

    @Autowired
    private ComponenteCurricularRepository componenteRepository;

    @Autowired
    private ComponenteCurricularContextoRepository contextoRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @Test
    void importsRecommendsSavesAndIdempotentlyReplacesOnePeriod() {
        List<ComponenteCurricular> components = componenteRepository.saveAll(List.of(
            component("MAT100", "Matemática I"),
            component("INF100", "Introdução à Computação")
        ));
        Curso course = cursoRepository.save(Curso.builder().nome("Sistemas de Informação").build());
        contextoRepository.saveAll(components.stream()
            .map(component -> ComponenteCurricularContexto.builder()
                .componenteCurricular(component)
                .sourceUrl("https://ementas.test/api")
                .sourceKey("test:" + component.getCodigo())
                .courseName(course.getNome())
                .isActive(true)
                .build())
            .toList());
        Usuario user = usuarioRepository.save(Usuario.builder()
            .nome("Pessoa Teste")
            .email("flow-test@radar.local")
            .senha("not-used-in-this-test")
            .limiteMatricula(5)
            .perfilInicial(1)
            .periodosRegularesCursados(3)
            .periodoAtual(4)
            .statusFormando(false)
            .turnosLivres(List.of(true, true, true))
            .curso(course)
            .build());

        OfertaTurmaInputDTO math = offering(
            "2026.2-MAT100-T01", "MAT100", "T01", Map.of("SEG", "08:00-10:00")
        );
        OfertaTurmaInputDTO computing = offering(
            "2026.2-INF100-T01", "INF100", "T01", Map.of("TER", "08:00-10:00")
        );
        ImportacaoOfertasResultadoDTO first = ofertaService.importPeriod(
            new ImportarOfertasDTO("test-source", "2026.2", true, List.of(math, computing))
        );
        assertThat(first.criadas()).isEqualTo(2);
        assertThat(first.desativadas()).isZero();

        List<RecomendacaoTurmaDTO> recommendations = recomendacaoService.recomendar(user.getId(), "busca");
        assertThat(recommendations).hasSize(2);
        List<Long> classIds = recommendations.stream()
            .map(item -> item.getTurma().getId())
            .toList();

        SimulacaoGradeDTO saved = simulacaoService.save(
            new SalvarSimulacaoGradeDTO(user.getId(), "Minha grade", "busca", classIds)
        );
        assertThat(saved.turmas()).extracting(item -> item.getId()).containsExactlyElementsOf(classIds);
        assertThat(simulacaoService.listByUser(user.getId())).hasSize(1);

        ImportacaoOfertasResultadoDTO repeated = ofertaService.importPeriod(
            new ImportarOfertasDTO("test-source", "2026.2", true, List.of(math, computing))
        );
        assertThat(repeated.criadas()).isZero();
        assertThat(repeated.atualizadas()).isEqualTo(2);
        assertThat(repeated.desativadas()).isZero();

        ImportacaoOfertasResultadoDTO replacement = ofertaService.importPeriod(
            new ImportarOfertasDTO("test-source", "2026.2", true, List.of(math))
        );
        assertThat(replacement.criadas()).isZero();
        assertThat(replacement.desativadas()).isEqualTo(1);
        assertThat(turmaRepository.findAllActiveWithDetails()).hasSize(1);
    }

    @Test
    void recommendsOnlyExplicitlyEquivalentOutsideCurriculumAsPriorityFive() {
        ComponenteCurricular curricular = componenteRepository.save(component("BASE100", "Base"));
        componenteRepository.save(component("EQV100", "Equivalente"));
        Curso course = cursoRepository.save(Curso.builder().nome("Curso de Equivalência").build());
        contextoRepository.save(ComponenteCurricularContexto.builder()
            .componenteCurricular(curricular)
            .sourceUrl("https://ementas.test/api")
            .sourceKey("equivalence:BASE100")
            .courseName(course.getNome())
            .recommendedPeriod(4)
            .isRequired(true)
            .isActive(true)
            .build());
        Usuario user = usuarioRepository.save(Usuario.builder()
            .nome("Pessoa Equivalência")
            .email("equivalence-test@radar.local")
            .senha("not-used-in-this-test")
            .limiteMatricula(5)
            .turnosLivres(List.of(true, true, true))
            .perfilInicial(1)
            .periodosRegularesCursados(3)
            .periodoAtual(4)
            .statusFormando(false)
            .curso(course)
            .build());

        OfertaTurmaInputDTO base = offering(
            "2026.2-BASE100-T01", "BASE100", "T01", Map.of("SEG", "08:00-10:00"),
            Set.of("EQV100")
        );
        OfertaTurmaInputDTO equivalent = offering(
            "2026.2-EQV100-T01", "EQV100", "T01", Map.of("TER", "08:00-10:00")
        );
        ofertaService.importPeriod(new ImportarOfertasDTO(
            "equivalence-test", "2026.2", true, List.of(base, equivalent)
        ));

        assertThat(recomendacaoService.recomendar(user.getId(), "busca"))
            .extracting(RecomendacaoTurmaDTO::getPrioridadeMatricula)
            .containsExactly(1, 5);
    }

    private static ComponenteCurricular component(String code, String name) {
        return ComponenteCurricular.builder()
            .codigo(code)
            .nome(name)
            .nivel((short) 1)
            .tipo("Obrigatória")
            .build();
    }

    private static OfertaTurmaInputDTO offering(
        String externalKey,
        String componentCode,
        String number,
        Map<String, String> schedules
    ) {
        return new OfertaTurmaInputDTO(
            externalKey,
            componentCode,
            componentCode + " - disciplina de teste",
            number,
            "Docente Teste",
            "UFBA",
            (byte) 1,
            "Matutino",
            schedules,
            (short) 40,
            (short) 10,
            Map.of()
        );
    }

    private static OfertaTurmaInputDTO offering(
        String externalKey,
        String componentCode,
        String number,
        Map<String, String> schedules,
        Set<String> equivalences
    ) {
        return new OfertaTurmaInputDTO(
            externalKey,
            componentCode,
            componentCode + " - disciplina de teste",
            number,
            "Docente Teste",
            "UFBA",
            (byte) 1,
            "Matutino",
            schedules,
            (short) 40,
            (short) 10,
            Map.of(),
            equivalences
        );
    }
}
