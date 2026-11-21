-- V05: integridad referencial y restricciones de dominio (requerimiento 6.5 y 6.12 del enunciado).
-- Se agregan las claves foráneas que faltaban y CHECK sobre cantidades, precios y valores.
-- NO se agrega FK despacho_detalle.formula_detalle_id: al editar una fórmula (aún pendiente) sus líneas
-- se recrean y el despacho se resincroniza en la misma transacción.

ALTER TABLE medico MODIFY especializacion_id BIGINT NOT NULL;
ALTER TABLE medico ADD CONSTRAINT fk_medico_especializacion FOREIGN KEY (especializacion_id) REFERENCES especializacion (id);
ALTER TABLE cita ADD CONSTRAINT fk_cita_especializacion FOREIGN KEY (especializacion_id) REFERENCES especializacion (id);
ALTER TABLE atencion_medica ADD CONSTRAINT fk_atencion_tarifa FOREIGN KEY (tarifa_id) REFERENCES tarifa (id);
ALTER TABLE formula_medica ADD CONSTRAINT fk_formula_atencion FOREIGN KEY (atencion_id) REFERENCES atencion_medica (id);
ALTER TABLE formula_medica ADD CONSTRAINT fk_formula_medico FOREIGN KEY (medico_id) REFERENCES medico (id);
ALTER TABLE factura ADD CONSTRAINT fk_factura_atencion FOREIGN KEY (atencion_id) REFERENCES atencion_medica (id);
ALTER TABLE factura ADD CONSTRAINT fk_factura_especializacion FOREIGN KEY (especializacion_id) REFERENCES especializacion (id);
ALTER TABLE factura_detalle ADD CONSTRAINT fk_fdetalle_tarifa FOREIGN KEY (tarifa_id) REFERENCES tarifa (id);
ALTER TABLE factura_detalle ADD CONSTRAINT fk_fdetalle_medicamento FOREIGN KEY (medicamento_id) REFERENCES medicamento (id);
ALTER TABLE movimiento_inventario ADD CONSTRAINT fk_mov_formula FOREIGN KEY (formula_medica_id) REFERENCES formula_medica (id);
ALTER TABLE movimiento_inventario ADD CONSTRAINT fk_mov_despacho FOREIGN KEY (despacho_id) REFERENCES despacho (id);
ALTER TABLE movimiento_inventario ADD CONSTRAINT fk_mov_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id);
ALTER TABLE pago ADD CONSTRAINT fk_pago_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id);
ALTER TABLE despacho ADD CONSTRAINT fk_despacho_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id);
ALTER TABLE carga_masiva ADD CONSTRAINT fk_carga_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id);
ALTER TABLE tarifa_log ADD CONSTRAINT fk_tlog_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id);

ALTER TABLE medicamento ADD CONSTRAINT ck_medicamento_stock CHECK (stock_actual >= 0);
ALTER TABLE medicamento ADD CONSTRAINT ck_medicamento_precios CHECK (precio_compra >= 0 AND precio_venta >= 0);
ALTER TABLE despacho_detalle ADD CONSTRAINT ck_ddet_cantidades CHECK (cantidad_entregada >= 0 AND cantidad_entregada <= cantidad_solicitada);
ALTER TABLE pago ADD CONSTRAINT ck_pago_valor CHECK (valor > 0);
ALTER TABLE factura ADD CONSTRAINT ck_factura_total CHECK (subtotal >= 0 AND impuestos >= 0 AND descuento >= 0 AND total >= 0);
