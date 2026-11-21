-- V04: ajustes para alinear el modelo con el enunciado del examen final.
--  * Utensilios sin vencimiento: lote_medicamento.fecha_vencimiento admite NULL (RN-INV: "lotes y vencimientos cuando corresponda").
--  * Regla 5.8-2: el medico crea Y CONFIRMA la formula. La formula nace en BORRADOR; el despacho solo existe al confirmarla.
--  * Req. 1.3: cantidad total requerida calculada = unidades_por_dosis x dosis_por_dia x duracion_dias.

ALTER TABLE lote_medicamento MODIFY fecha_vencimiento DATE NULL;

ALTER TABLE formula_medica
    ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    ADD COLUMN fecha_confirmacion DATETIME NULL;
-- Las formulas existentes ya tienen despacho: se consideran confirmadas.
UPDATE formula_medica SET estado = 'CONFIRMADA', fecha_confirmacion = fecha_emision;

ALTER TABLE formula_medica_detalle
    ADD COLUMN unidades_por_dosis INT NULL,
    ADD COLUMN dosis_por_dia INT NULL;
