-- Agrega la asignación opcional de asesor a alumnos existentes.
-- Ejecuta esta migración en la base de residencias después de revisar el esquema.
-- Es idempotente: conserva los registros y las asignaciones si ya existen.

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'alumnos'
          AND column_name = 'id_asesor'
    ) THEN
        ALTER TABLE alumnos ADD COLUMN id_asesor BIGINT;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.key_column_usage source_key
        JOIN information_schema.constraint_column_usage target_key
          USING (constraint_catalog, constraint_schema, constraint_name)
        WHERE source_key.table_schema = current_schema()
          AND source_key.table_name = 'alumnos'
          AND source_key.column_name = 'id_asesor'
          AND target_key.table_schema = current_schema()
          AND target_key.table_name = 'asesores_academicos'
          AND target_key.column_name = 'id_asesor'
    ) THEN
        ALTER TABLE alumnos
            ADD CONSTRAINT fk_alumnos_asesor
            FOREIGN KEY (id_asesor)
            REFERENCES asesores_academicos (id_asesor);
    END IF;
END
$$;
