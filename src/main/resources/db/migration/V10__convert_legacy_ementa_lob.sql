-- Older Hibernate versions mapped @Lob String to PostgreSQL large-object OIDs.
-- Convert the contents (not the numeric OID) before validating the TEXT model.
DO $$
DECLARE
    current_type TEXT;
BEGIN
    SELECT data_type
      INTO current_type
      FROM information_schema.columns
     WHERE table_schema = current_schema()
       AND table_name = 'componentes_curriculares'
       AND column_name = 'ementa';

    IF current_type = 'oid' THEN
        CREATE TEMP TABLE radar_legacy_ementa_oids ON COMMIT DROP AS
            SELECT DISTINCT ementa AS oid
              FROM componentes_curriculares
             WHERE ementa IS NOT NULL;

        ALTER TABLE componentes_curriculares
            ALTER COLUMN ementa TYPE TEXT
            USING CASE
                WHEN ementa IS NULL THEN NULL
                ELSE convert_from(lo_get(ementa), 'UTF8')
            END;

        PERFORM lo_unlink(oid) FROM radar_legacy_ementa_oids;
    END IF;
END
$$;
