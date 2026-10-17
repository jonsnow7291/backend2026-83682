-- ==========================================================
-- Migración: columnas nuevas de formula_medica (Integrante 3)
-- Ejecutar SOLO si la tabla formula_medica ya existía antes de
-- este cambio (schema.sql ya las incluye para instalaciones nuevas).
-- ==========================================================
USE clinica;

ALTER TABLE formula_medica
    ADD COLUMN frecuencia VARCHAR(100) NOT NULL DEFAULT 'Sin especificar' AFTER dosis,
    ADD COLUMN duracion_dias INT NOT NULL DEFAULT 1 AFTER frecuencia,
    ADD COLUMN fecha_emision DATETIME NULL AFTER indicaciones;

UPDATE formula_medica SET fecha_emision = fecha_creacion_registro WHERE fecha_emision IS NULL;

ALTER TABLE formula_medica MODIFY COLUMN fecha_emision DATETIME NOT NULL;
