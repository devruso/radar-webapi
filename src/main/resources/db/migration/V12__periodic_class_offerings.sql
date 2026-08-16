ALTER TABLE turmas
    ADD COLUMN periodo_letivo VARCHAR(20),
    ADD COLUMN source VARCHAR(100),
    ADD COLUMN external_key VARCHAR(160),
    ADD COLUMN ativa BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN updated_at TIMESTAMPTZ,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

CREATE UNIQUE INDEX uq_turmas_source_external_key
    ON turmas (source, external_key)
    WHERE source IS NOT NULL AND external_key IS NOT NULL;

CREATE INDEX idx_turmas_periodo_ativas
    ON turmas (periodo_letivo, ativa);
