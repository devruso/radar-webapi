package com.jangada.RADAR.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jangada.RADAR.mappers.TurmaMapper;
import com.jangada.RADAR.models.dtos.ImportacaoOfertasResultadoDTO;
import com.jangada.RADAR.models.dtos.ImportarOfertasDTO;
import com.jangada.RADAR.models.dtos.OfertaTurmaInputDTO;
import com.jangada.RADAR.models.entities.ComponenteCurricular;
import com.jangada.RADAR.models.entities.Horario;
import com.jangada.RADAR.models.entities.Turma;
import com.jangada.RADAR.models.entities.Vagas;
import com.jangada.RADAR.repositories.ComponenteCurricularRepository;
import com.jangada.RADAR.repositories.TurmaRepository;
import com.jangada.RADAR.utils.GradeOptimizer;
import com.jangada.RADAR.utils.RecomendacaoUtil;

@Service
public class OfertaTurmaService {

    private static final Set<String> VALID_DAYS = Set.of("SEG", "TER", "QUA", "QUI", "SEX", "SAB");

    private final TurmaRepository turmaRepository;
    private final ComponenteCurricularRepository componenteRepository;
    private final String importKey;

    public OfertaTurmaService(
        TurmaRepository turmaRepository,
        ComponenteCurricularRepository componenteRepository,
        @Value("${radar.ofertas.import-key:${radar.catalog.sync-key:}}") String importKey
    ) {
        this.turmaRepository = turmaRepository;
        this.componenteRepository = componenteRepository;
        this.importKey = importKey == null ? "" : importKey;
    }

    public boolean isAuthorized(String suppliedKey) {
        if (importKey.isBlank()) {
            return false;
        }
        return suppliedKey != null && MessageDigest.isEqual(
            importKey.getBytes(StandardCharsets.UTF_8),
            suppliedKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Replaces one source/period atomically. Repeating the same payload updates
     * the same rows instead of creating duplicates.
     */
    @Transactional
    public synchronized ImportacaoOfertasResultadoDTO importPeriod(ImportarOfertasDTO request) {
        String source = normalizeSource(request.source());
        String period = request.periodoLetivo().trim();

        Map<String, OfertaTurmaInputDTO> inputByKey = new LinkedHashMap<>();
        for (OfertaTurmaInputDTO input : request.ofertas()) {
            String key = input.externalKey().trim();
            if (inputByKey.putIfAbsent(key, input) != null) {
                throw new IllegalArgumentException("externalKey duplicada no lote: " + key);
            }
        }

        Set<String> componentCodes = inputByKey.values().stream()
            .map(item -> normalizeCode(item.componenteCodigo()))
            .collect(Collectors.toCollection(HashSet::new));
        Map<String, ComponenteCurricular> components = componenteRepository
            .findAllByCodigoIn(new ArrayList<>(componentCodes)).stream()
            .collect(Collectors.toMap(
                item -> normalizeCode(item.getCodigo()),
                Function.identity()
            ));
        Set<String> missingCodes = new HashSet<>(componentCodes);
        missingCodes.removeAll(components.keySet());
        if (!missingCodes.isEmpty()) {
            List<ComponenteCurricular> discovered = missingCodes.stream()
                .sorted()
                .map(code -> createDiscoveredComponent(code, inputByKey.values()))
                .toList();
            componenteRepository.saveAll(discovered).forEach(component ->
                components.put(normalizeCode(component.getCodigo()), component)
            );
            componenteRepository.flush();
        }
        boolean enrichedEquivalences = false;
        for (OfertaTurmaInputDTO input : inputByKey.values()) {
            if (input.equivalencias() == null || input.equivalencias().isEmpty()) {
                continue;
            }
            ComponenteCurricular component = components.get(normalizeCode(input.componenteCodigo()));
            Set<String> merged = new HashSet<>(
                RecomendacaoUtil.extrairCodigosComponente(component.getEquivalencias())
            );
            input.equivalencias().stream()
                .filter(code -> code != null && !code.isBlank())
                .map(OfertaTurmaService::normalizeCode)
                .filter(code -> !code.equals(normalizeCode(component.getCodigo())))
                .forEach(merged::add);
            component.setEquivalencias(merged.stream().sorted().collect(Collectors.joining(",")));
            enrichedEquivalences = true;
        }
        if (enrichedEquivalences) {
            componenteRepository.saveAll(components.values());
            componenteRepository.flush();
        }

        List<String> externalKeys = new ArrayList<>(inputByKey.keySet());
        Map<String, Turma> existingByKey = turmaRepository
            .findAllBySourceAndExternalKeyIn(source, externalKeys).stream()
            .collect(Collectors.toMap(Turma::getExternalKey, Function.identity()));

        Set<Long> previouslyActiveIds = new HashSet<>();
        if (request.substituirPeriodo()) {
            List<Turma> current = turmaRepository
                .findAllBySourceAndPeriodoLetivoAndAtivaTrue(source, period);
            current.forEach(item -> {
                previouslyActiveIds.add(item.getId());
                item.setAtiva(false);
                item.setUpdatedAt(Instant.now());
            });
        }

        int created = 0;
        List<Turma> imported = new ArrayList<>(inputByKey.size());
        Instant now = Instant.now();
        for (Map.Entry<String, OfertaTurmaInputDTO> entry : inputByKey.entrySet()) {
            OfertaTurmaInputDTO input = entry.getValue();
            Turma offering = existingByKey.get(entry.getKey());
            if (offering == null) {
                offering = Turma.builder().source(source).externalKey(entry.getKey()).build();
                created++;
            }

            Horario schedule = offering.getHorario();
            if (schedule == null) {
                schedule = new Horario();
            }
            schedule.setCodigo(entry.getKey());
            schedule.setTurno(input.turno().trim());
            schedule.setHorarios(normalizeSchedule(input.horarios()));
            if (!GradeOptimizer.isValidSchedule(schedule)) {
                throw new IllegalArgumentException(
                    "Horário inválido para a oferta " + entry.getKey()
                        + ". Use dias SEG-SAB e intervalos HH:mm-HH:mm."
                );
            }

            Vagas vacancies = offering.getVagas();
            if (vacancies == null) {
                vacancies = new Vagas();
            }
            vacancies.setTotalVagas(input.totalVagas());
            vacancies.setVagasDisponiveis(input.vagasDisponiveis());
            vacancies.setReservaVagas(normalizeReservations(input.reservas()));

            offering.setComponenteCurricular(components.get(normalizeCode(input.componenteCodigo())));
            offering.setNumero(input.numero().trim());
            offering.setProfessor(input.professor().trim());
            offering.setLocal(input.local().trim());
            offering.setTipo(input.tipo() == null ? (byte) 1 : input.tipo());
            offering.setPeriodoLetivo(period);
            offering.setSource(source);
            offering.setExternalKey(entry.getKey());
            offering.setAtiva(true);
            offering.setUpdatedAt(now);
            offering.setHorario(schedule);
            offering.setVagas(vacancies);
            imported.add(offering);
            if (offering.getId() != null) {
                previouslyActiveIds.remove(offering.getId());
            }
        }

        List<Turma> persisted = turmaRepository.saveAll(imported);
        turmaRepository.flush();
        return new ImportacaoOfertasResultadoDTO(
            imported.size(),
            created,
            imported.size() - created,
            previouslyActiveIds.size(),
            persisted.stream().map(TurmaMapper::toDto).toList()
        );
    }

    private static Map<String, String> normalizeSchedule(Map<String, String> raw) {
        Map<String, String> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : raw.entrySet()) {
            String day = normalizeDay(entry.getKey());
            if (!VALID_DAYS.contains(day)) {
                throw new IllegalArgumentException("Dia de horário inválido: " + entry.getKey());
            }
            String intervals = entry.getValue() == null ? "" : entry.getValue().trim();
            if (intervals.isEmpty() || normalized.putIfAbsent(day, intervals) != null) {
                throw new IllegalArgumentException("Horário ausente ou dia repetido: " + day);
            }
        }
        return normalized;
    }

    private static Map<String, Integer> normalizeReservations(Map<String, Integer> raw) {
        if (raw == null || raw.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, Integer> normalized = new LinkedHashMap<>();
        raw.forEach((name, quantity) -> {
            if (name == null || name.isBlank() || quantity == null || quantity < 0) {
                throw new IllegalArgumentException("Reserva de vagas inválida");
            }
            normalized.put(name.trim(), quantity);
        });
        return normalized;
    }

    private static String normalizeDay(String raw) {
        if (raw == null) {
            return "";
        }
        String day = Normalizer.normalize(raw, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .trim()
            .toUpperCase(Locale.ROOT);
        return day.length() < 3 ? day : day.substring(0, 3);
    }

    private static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private static ComponenteCurricular createDiscoveredComponent(
        String code,
        Iterable<OfertaTurmaInputDTO> offerings
    ) {
        for (OfertaTurmaInputDTO offering : offerings) {
            if (code.equals(normalizeCode(offering.componenteCodigo()))) {
                String name = offering.componenteNome();
                if (name == null || name.isBlank()) {
                    throw new IllegalArgumentException(
                        "Componente " + code + " não existe no Ementas e veio sem nome do SIGAA"
                    );
                }
                return ComponenteCurricular.builder()
                    .codigo(code)
                    .nome(name.trim())
                    .tipo("Componente identificado em oferta SIGAA")
                    .build();
            }
        }
        throw new IllegalArgumentException("Componente ausente no lote: " + code);
    }

    private static String normalizeSource(String source) {
        return source.trim().toLowerCase(Locale.ROOT);
    }
}
