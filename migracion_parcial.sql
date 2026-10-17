-- ==========================================================
-- Migración del segundo parcial
-- Ejecutar SOLO sobre una base de datos creada con una versión anterior
-- de schema.sql (schema.sql ya incluye estas columnas para instalaciones nuevas).
-- ==========================================================
USE clinica;

-- Fórmulas médicas: frecuencia, duración y fecha de emisión
ALTER TABLE formula_medica
    ADD COLUMN frecuencia VARCHAR(100) NOT NULL DEFAULT 'Sin especificar' AFTER dosis,
    ADD COLUMN duracion_dias INT NOT NULL DEFAULT 1 AFTER frecuencia,
    ADD COLUMN fecha_emision DATETIME NULL AFTER indicaciones;

UPDATE formula_medica SET fecha_emision = fecha_creacion_registro WHERE fecha_emision IS NULL;

ALTER TABLE formula_medica MODIFY COLUMN fecha_emision DATETIME NOT NULL;

-- Historias clínicas: cita de origen, diagnóstico y observaciones
ALTER TABLE historia_medica
    ADD COLUMN cita_id BIGINT NULL AFTER paciente_id,
    ADD COLUMN diagnostico TEXT NULL AFTER cita_id,
    ADD COLUMN observaciones TEXT NULL AFTER diagnostico,
    ADD CONSTRAINT fk_historia_cita FOREIGN KEY (cita_id) REFERENCES cita (id);

-- Anotaciones: observación y diagnóstico/evolución
ALTER TABLE anotacion_historia
    ADD COLUMN observacion TEXT NULL AFTER descripcion,
    ADD COLUMN diagnostico TEXT NULL AFTER observacion;
