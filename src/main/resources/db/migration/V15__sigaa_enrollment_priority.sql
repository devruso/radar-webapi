-- Student attributes required by the official SIGAA enrollment-priority legend.
-- `periodos_regulares_cursados` already excludes suspended, locked and mobility
-- semesters, exactly as specified by paragraph 1 of that legend.
ALTER TABLE usuarios
    ADD COLUMN perfil_inicial INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN periodos_regulares_cursados INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN coeficiente_rendimento NUMERIC(4, 2),
    ADD COLUMN status_formando BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE usuarios
SET periodos_regulares_cursados = GREATEST(COALESCE(periodo_atual, 1) - perfil_inicial, 0),
    periodo_atual = perfil_inicial
        + GREATEST(COALESCE(periodo_atual, 1) - perfil_inicial, 0);

ALTER TABLE usuarios
    ADD CONSTRAINT chk_usuarios_perfil_inicial CHECK (perfil_inicial >= 1),
    ADD CONSTRAINT chk_usuarios_periodos_regulares CHECK (periodos_regulares_cursados >= 0),
    ADD CONSTRAINT chk_usuarios_cr CHECK (
        coeficiente_rendimento IS NULL
        OR coeficiente_rendimento BETWEEN 0 AND 10
    );

-- RADAR-owned projection for explicit equivalence evidence. The current
-- Ementas list endpoint does not expose relations, so this field may also be
-- enriched by the RADAR SIGAA importer without changing Ementas itself.
ALTER TABLE componentes_curriculares
    ADD COLUMN equivalencias TEXT;
