package com.jangada.RADAR.integrations.ementas;

import java.util.List;

public final class EmentasApiModels {

    private EmentasApiModels() {
    }

    public record LoginResponse(String accessToken, String token) {
        public String bearerToken() {
            return hasText(accessToken) ? accessToken : token;
        }
    }

    public record Page(List<Component> results, int total, PageMeta meta) {
    }

    public record PageMeta(int page, int limit, int total, int totalPages) {
    }

    public record Component(
        String id,
        String code,
        String name,
        String department,
        String modality,
        String program,
        String semester,
        String academicLevel,
        String prerequeriments,
        String methodology,
        String objective,
        String syllabus,
        String learningAssessment,
        String bibliography,
        String referencesBasic,
        String referencesComplementary,
        String updatedAt,
        Workload workload,
        List<CurriculumContext> curriculumContexts,
        List<ComponentRelation> relations
    ) {
        public List<CurriculumContext> safeCurriculumContexts() {
            return curriculumContexts == null ? List.of() : curriculumContexts;
        }

        public List<ComponentRelation> safeRelations() {
            return relations == null ? List.of() : relations;
        }
    }

    public record ComponentRelation(String relationType, String relatedCode) {
        public boolean equivalence() {
            return "equivalence".equalsIgnoreCase(relationType);
        }
    }

    public record Workload(
        Integer studentTheory,
        Integer studentPractice,
        Integer studentTheoryPractice,
        Integer studentInternship,
        Integer studentPracticeInternship,
        Integer studentExtension
    ) {
        public int totalStudentHours() {
            return value(studentTheory)
                + value(studentPractice)
                + value(studentTheoryPractice)
                + value(studentInternship)
                + value(studentPracticeInternship)
                + value(studentExtension);
        }

        private static int value(Integer number) {
            return number == null ? 0 : number;
        }
    }

    public record CurriculumContext(
        String id,
        String sourceKey,
        String curriculumCode,
        String curriculumName,
        String courseName,
        String implementationSemester,
        Integer recommendedPeriod,
        Boolean isRequired,
        Boolean isActive,
        String prerequeriments,
        String academicLevel
    ) {
        public boolean required() {
            return Boolean.TRUE.equals(isRequired);
        }

        public boolean active() {
            return Boolean.TRUE.equals(isActive);
        }
    }

    public record SourcedComponent(String sourceUrl, Component component) {
    }

    public record SourceStatus(String sourceUrl, int components, String error) {
        public boolean successful() {
            return error == null;
        }
    }

    public record CatalogSnapshot(
        List<SourcedComponent> components,
        List<SourceStatus> sources
    ) {
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
