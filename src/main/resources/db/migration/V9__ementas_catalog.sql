-- RADAR owns this local projection. Ementas remains an upstream read-only
-- source and can be temporarily incomplete without causing local deletions.

ALTER TABLE componentes_curriculares
    ALTER COLUMN prerequisito TYPE TEXT,
    ADD COLUMN ementas_external_id VARCHAR(255),
    ADD COLUMN departamento VARCHAR(255),
    ADD COLUMN nivel_academico VARCHAR(50),
    ADD COLUMN semestre VARCHAR(50),
    ADD COLUMN programa TEXT,
    ADD COLUMN objetivo TEXT,
    ADD COLUMN metodologia TEXT,
    ADD COLUMN avaliacao_aprendizagem TEXT,
    ADD COLUMN bibliografia TEXT,
    ADD COLUMN carga_horaria INTEGER,
    ADD COLUMN ementas_sources TEXT,
    ADD COLUMN ementas_updated_at TIMESTAMPTZ,
    ADD COLUMN ementas_synced_at TIMESTAMPTZ;

CREATE INDEX idx_componentes_ementas_synced_at
    ON componentes_curriculares (ementas_synced_at);

CREATE TABLE componente_contextos_curriculares (
    id BIGSERIAL PRIMARY KEY,
    componente_id BIGINT NOT NULL
        REFERENCES componentes_curriculares(id) ON DELETE CASCADE,
    ementas_external_id VARCHAR(255),
    source_url VARCHAR(500) NOT NULL,
    source_key VARCHAR(500) NOT NULL,
    curriculum_code VARCHAR(255),
    curriculum_name TEXT,
    course_name TEXT,
    implementation_semester VARCHAR(50),
    recommended_period INTEGER,
    is_required BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    prerequeriments TEXT,
    academic_level VARCHAR(50),
    synced_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_componente_contexto_source
        UNIQUE (componente_id, source_url, source_key)
);

CREATE INDEX idx_componente_contextos_componente
    ON componente_contextos_curriculares (componente_id);

CREATE INDEX idx_componente_contextos_curriculum
    ON componente_contextos_curriculares (curriculum_code);
