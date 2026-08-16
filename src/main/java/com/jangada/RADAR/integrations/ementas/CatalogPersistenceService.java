package com.jangada.RADAR.integrations.ementas;

import java.text.Normalizer;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jangada.RADAR.integrations.ementas.EmentasApiModels.CatalogSnapshot;
import com.jangada.RADAR.integrations.ementas.EmentasApiModels.Component;
import com.jangada.RADAR.integrations.ementas.EmentasApiModels.ComponentRelation;
import com.jangada.RADAR.integrations.ementas.EmentasApiModels.CurriculumContext;
import com.jangada.RADAR.integrations.ementas.EmentasApiModels.SourcedComponent;
import com.jangada.RADAR.models.entities.ComponenteCurricular;
import com.jangada.RADAR.models.entities.ComponenteCurricularContexto;
import com.jangada.RADAR.repositories.ComponenteCurricularContextoRepository;
import com.jangada.RADAR.repositories.ComponenteCurricularRepository;

@Service
public class CatalogPersistenceService {

    private final ComponenteCurricularRepository componenteRepository;
    private final ComponenteCurricularContextoRepository contextoRepository;

    public CatalogPersistenceService(
        ComponenteCurricularRepository componenteRepository,
        ComponenteCurricularContextoRepository contextoRepository
    ) {
        this.componenteRepository = componenteRepository;
        this.contextoRepository = contextoRepository;
    }

    @Transactional
    public PersistenceResult persist(CatalogSnapshot snapshot, Instant syncedAt) {
        Map<String, List<SourcedComponent>> byCode = snapshot.components().stream()
            .filter(item -> item.component() != null && hasText(item.component().code()))
            .collect(Collectors.groupingBy(
                item -> normalizeCode(item.component().code()),
                LinkedHashMap::new,
                Collectors.toList()
            ));

        List<String> codes = new ArrayList<>(byCode.keySet());
        if (codes.isEmpty()) {
            return new PersistenceResult(0, 0, 0, 0, 0);
        }
        Map<String, ComponenteCurricular> existingByCode = componenteRepository
            .findAllByCodigoIn(codes).stream()
            .collect(Collectors.toMap(
                item -> normalizeCode(item.getCodigo()),
                Function.identity()
            ));

        int created = 0;
        List<ComponenteCurricular> componentsToSave = new ArrayList<>();
        for (Map.Entry<String, List<SourcedComponent>> entry : byCode.entrySet()) {
            ComponenteCurricular target = existingByCode.get(entry.getKey());
            if (target == null) {
                target = ComponenteCurricular.builder().codigo(entry.getKey()).build();
                created++;
            }
            mergeComponent(target, entry.getValue(), syncedAt);
            componentsToSave.add(target);
        }

        List<ComponenteCurricular> persistedComponents = componenteRepository.saveAll(componentsToSave);
        componenteRepository.flush();
        Map<String, ComponenteCurricular> persistedByCode = persistedComponents.stream()
            .collect(Collectors.toMap(
                item -> normalizeCode(item.getCodigo()),
                Function.identity()
            ));

        List<Long> componentIds = persistedComponents.stream()
            .map(ComponenteCurricular::getId)
            .filter(Objects::nonNull)
            .toList();
        Map<String, ComponenteCurricularContexto> existingContexts = contextoRepository
            .findAllByComponenteCurricularIdIn(componentIds).stream()
            .collect(Collectors.toMap(
                item -> contextKey(
                    item.getComponenteCurricular().getId(),
                    item.getSourceUrl(),
                    item.getSourceKey()
                ),
                Function.identity()
            ));

        int createdContexts = 0;
        Map<String, ComponenteCurricularContexto> contextsToSave = new LinkedHashMap<>();
        for (Map.Entry<String, List<SourcedComponent>> entry : byCode.entrySet()) {
            ComponenteCurricular component = persistedByCode.get(entry.getKey());
            for (SourcedComponent sourced : entry.getValue()) {
                for (CurriculumContext remote : sourced.component().safeCurriculumContexts()) {
                    if (!hasText(remote.sourceKey())) {
                        continue;
                    }
                    String key = contextKey(component.getId(), sourced.sourceUrl(), remote.sourceKey());
                    ComponenteCurricularContexto target = existingContexts.get(key);
                    if (target == null) {
                        target = ComponenteCurricularContexto.builder()
                            .componenteCurricular(component)
                            .sourceUrl(sourced.sourceUrl())
                            .sourceKey(remote.sourceKey())
                            .build();
                        createdContexts++;
                    }
                    mergeContext(target, remote, syncedAt);
                    contextsToSave.put(key, target);
                }
            }
        }
        contextoRepository.saveAll(contextsToSave.values());

        return new PersistenceResult(
            byCode.size(),
            created,
            byCode.size() - created,
            contextsToSave.size(),
            createdContexts
        );
    }

    private static void mergeComponent(
        ComponenteCurricular target,
        List<SourcedComponent> sourcedComponents,
        Instant syncedAt
    ) {
        List<SourcedComponent> richestFirst = sourcedComponents.stream()
            .sorted(Comparator.comparingInt(CatalogPersistenceService::richness).reversed())
            .toList();
        Component canonical = richestFirst.get(0).component();

        setIfText(firstText(richestFirst, Component::name), target::setNome);
        setIfText(firstText(richestFirst, Component::syllabus), target::setEmenta);
        setIfText(firstText(richestFirst, Component::department), target::setDepartamento);
        setIfText(firstText(richestFirst, Component::academicLevel), target::setNivelAcademico);
        setIfText(firstText(richestFirst, Component::semester), target::setSemestre);
        setIfText(firstText(richestFirst, Component::program), target::setPrograma);
        setIfText(firstText(richestFirst, Component::objective), target::setObjetivo);
        setIfText(firstText(richestFirst, Component::methodology), target::setMetodologia);
        setIfText(firstText(richestFirst, Component::learningAssessment), target::setAvaliacaoAprendizagem);
        setIfText(firstText(richestFirst, CatalogPersistenceService::bibliography), target::setBibliografia);
        setIfText(canonical.id(), target::setEmentasExternalId);

        String equivalences = sourcedComponents.stream()
            .map(SourcedComponent::component)
            .flatMap(component -> component.safeRelations().stream())
            .filter(ComponentRelation::equivalence)
            .map(ComponentRelation::relatedCode)
            .filter(CatalogPersistenceService::hasText)
            .map(CatalogPersistenceService::normalizeCode)
            .distinct()
            .sorted()
            .collect(Collectors.joining(","));
        if (hasText(equivalences)) {
            target.setEquivalencias(equivalences);
        }

        String prerequeriments = firstText(richestFirst, Component::prerequeriments);
        List<CurriculumContext> allContexts = sourcedComponents.stream()
            .flatMap(item -> item.component().safeCurriculumContexts().stream())
            .toList();
        CurriculumContext siContext = selectInformationSystemsContext(allContexts);
        if (siContext != null) {
            if (siContext.recommendedPeriod() != null && siContext.recommendedPeriod() > 0) {
                target.setNivel(siContext.recommendedPeriod().shortValue());
            }
            target.setTipo(siContext.required() ? "Obrigatória" : "Optativa");
            if (hasMeaningfulPrerequisites(siContext.prerequeriments())) {
                prerequeriments = siContext.prerequeriments();
            }
        }
        if (hasMeaningfulPrerequisites(prerequeriments)) {
            target.setPrerequisito(prerequeriments.trim());
        }

        Integer workload = richestFirst.stream()
            .map(SourcedComponent::component)
            .map(Component::workload)
            .filter(Objects::nonNull)
            .map(EmentasApiModels.Workload::totalStudentHours)
            .filter(total -> total > 0)
            .findFirst()
            .orElse(null);
        if (workload != null) {
            target.setCargaHoraria(workload);
        }

        target.setEmentasSources(sourcedComponents.stream()
            .map(SourcedComponent::sourceUrl)
            .distinct()
            .sorted()
            .collect(Collectors.joining(",")));
        richestFirst.stream()
            .map(SourcedComponent::component)
            .map(Component::updatedAt)
            .map(CatalogPersistenceService::parseInstant)
            .filter(Objects::nonNull)
            .max(Comparator.naturalOrder())
            .ifPresent(target::setEmentasUpdatedAt);
        target.setEmentasSyncedAt(syncedAt);
    }

    private static void mergeContext(
        ComponenteCurricularContexto target,
        CurriculumContext remote,
        Instant syncedAt
    ) {
        setIfText(remote.id(), target::setEmentasExternalId);
        setIfText(remote.curriculumCode(), target::setCurriculumCode);
        setIfText(remote.curriculumName(), target::setCurriculumName);
        setIfText(remote.courseName(), target::setCourseName);
        setIfText(remote.implementationSemester(), target::setImplementationSemester);
        setIfText(remote.prerequeriments(), target::setPrerequeriments);
        setIfText(remote.academicLevel(), target::setAcademicLevel);
        target.setRecommendedPeriod(remote.recommendedPeriod());
        target.setRequired(remote.required());
        target.setActive(remote.active());
        target.setSyncedAt(syncedAt);
    }

    private static CurriculumContext selectInformationSystemsContext(List<CurriculumContext> contexts) {
        return contexts.stream()
            .filter(context -> normalizeText(context.courseName()).contains("SISTEMAS DE INFORMACAO"))
            .max(Comparator
                .comparing(CurriculumContext::active)
                .thenComparing(
                    CurriculumContext::implementationSemester,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ))
            .orElse(null);
    }

    private static int richness(SourcedComponent sourced) {
        Component component = sourced.component();
        int score = component.safeCurriculumContexts().size() * 10;
        score += hasText(component.name()) ? 1 : 0;
        score += hasText(component.department()) ? 1 : 0;
        score += hasText(component.program()) ? 1 : 0;
        score += hasText(component.syllabus()) ? 2 : 0;
        score += hasText(component.objective()) ? 1 : 0;
        score += hasText(component.methodology()) ? 1 : 0;
        score += component.workload() == null ? 0 : 2;
        return score;
    }

    private static String firstText(
        List<SourcedComponent> components,
        Function<Component, String> extractor
    ) {
        return components.stream()
            .map(SourcedComponent::component)
            .map(extractor)
            .filter(CatalogPersistenceService::hasText)
            .findFirst()
            .orElse(null);
    }

    private static String bibliography(Component component) {
        return Stream.of(
                component.bibliography(),
                component.referencesBasic(),
                component.referencesComplementary()
            )
            .filter(CatalogPersistenceService::hasText)
            .distinct()
            .collect(Collectors.joining("\n\n"));
    }

    private static boolean hasMeaningfulPrerequisites(String value) {
        if (!hasText(value)) {
            return false;
        }
        String normalized = normalizeText(value).replaceAll("[^A-Z0-9]", "");
        return !normalized.equals("NAOSEAPLICA")
            && !normalized.equals("NENHUM")
            && !normalized.equals("SEMPREREQUISITO");
    }

    private static String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toUpperCase(Locale.ROOT);
    }

    private static Instant parseInstant(String value) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static String contextKey(Long componentId, String sourceUrl, String sourceKey) {
        return componentId + "|" + sourceUrl + "|" + sourceKey;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static void setIfText(String value, java.util.function.Consumer<String> setter) {
        if (hasText(value)) {
            setter.accept(value.trim());
        }
    }

    public record PersistenceResult(
        int catalogComponents,
        int createdComponents,
        int updatedComponents,
        int synchronizedContexts,
        int createdContexts
    ) {
    }
}
