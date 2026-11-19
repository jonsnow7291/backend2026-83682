-- V02: modelo de datos de las Fases 1 a 5 (inventario, citas, atencion/facturacion, despacho).
-- Aplicar sobre una BD ya creada con schema.sql anterior. En una BD nueva, schema.sql ya lo incluye.

-- ==========================================================
-- FASE 1: INVENTARIO
-- ==========================================================
-- Decision: medicamento.stock_actual se mantiene SINCRONIZADO en la misma transaccion que cada
-- movimiento (suma de lotes). Evita sumar lotes en cada listado y permite filtrar agotados/stock bajo.
ALTER TABLE medicamento
    MODIFY fecha_compra DATE NULL,
    MODIFY fecha_vence DATE NULL,
    ADD COLUMN codigo VARCHAR(8) NULL,
    ADD COLUMN registro_invima VARCHAR(50) NULL,
    ADD COLUMN tipo VARCHAR(15) NOT NULL DEFAULT 'MEDICAMENTO',
    ADD COLUMN precio_compra DECIMAL(12,2) NOT NULL DEFAULT 0,
    ADD COLUMN precio_venta DECIMAL(12,2) NOT NULL DEFAULT 0,
    ADD COLUMN iva_porcentaje DECIMAL(5,2) NOT NULL DEFAULT 0,
    ADD COLUMN stock_actual INT NOT NULL DEFAULT 0,
    ADD COLUMN stock_minimo INT NOT NULL DEFAULT 0,
    ADD COLUMN fecha_ingreso DATE NULL,
    ADD COLUMN fecha_proximo_pedido DATE NULL,
    ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE medicamento SET codigo = CONCAT('MED', LPAD(id, 5, '0')), registro_invima = CONCAT('INVIMA-', LPAD(id, 4, '0')),
    fecha_ingreso = DATE(fecha_creacion_registro) WHERE codigo IS NULL;
ALTER TABLE medicamento ADD UNIQUE KEY uk_medicamento_codigo (codigo);

CREATE TABLE IF NOT EXISTS lote_medicamento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicamento_id BIGINT NOT NULL,
    numero_lote VARCHAR(50) NOT NULL,
    cantidad_inicial INT NOT NULL,
    cantidad_disponible INT NOT NULL,
    precio_compra DECIMAL(12,2) NOT NULL DEFAULT 0,
    fecha_compra DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    fecha_creacion DATETIME NOT NULL,
    CONSTRAINT uk_lote UNIQUE (medicamento_id, numero_lote),
    CONSTRAINT fk_lote_medicamento FOREIGN KEY (medicamento_id) REFERENCES medicamento (id),
    CONSTRAINT ck_lote_cantidad CHECK (cantidad_disponible >= 0)
);

CREATE TABLE IF NOT EXISTS movimiento_inventario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicamento_id BIGINT NOT NULL,
    lote_id BIGINT NULL,
    tipo VARCHAR(10) NOT NULL,
    cantidad INT NOT NULL,
    fecha DATETIME NOT NULL,
    usuario_id BIGINT NULL,
    formula_medica_id BIGINT NULL,
    despacho_id BIGINT NULL,
    motivo VARCHAR(255) NULL,
    CONSTRAINT fk_mov_medicamento FOREIGN KEY (medicamento_id) REFERENCES medicamento (id),
    CONSTRAINT fk_mov_lote FOREIGN KEY (lote_id) REFERENCES lote_medicamento (id),
    INDEX idx_mov_despacho (despacho_id),
    INDEX idx_mov_fecha (fecha)
);

CREATE TABLE IF NOT EXISTS carga_masiva (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_archivo VARCHAR(255) NOT NULL,
    usuario_id BIGINT NULL,
    fecha DATETIME NOT NULL,
    total_filas INT NOT NULL,
    filas_validas INT NOT NULL,
    filas_rechazadas INT NOT NULL
);

CREATE TABLE IF NOT EXISTS carga_masiva_detalle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    carga_id BIGINT NOT NULL,
    fila INT NOT NULL,
    codigo VARCHAR(50) NULL,
    resultado VARCHAR(15) NOT NULL,
    motivo VARCHAR(500) NULL,
    CONSTRAINT fk_carga_detalle FOREIGN KEY (carga_id) REFERENCES carga_masiva (id) ON DELETE CASCADE
);

-- ==========================================================
-- FASE 2: CITAS Y HORARIOS
-- ==========================================================
ALTER TABLE medico ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE especializacion ADD COLUMN activa BOOLEAN NOT NULL DEFAULT TRUE;

-- Estados de cita: PENDIENTE, CONFIRMADA, ATENDIDA, CANCELADA, FINALIZADA. fecha_hora = inicio de la cita.
ALTER TABLE cita
    ADD COLUMN fecha_hora_fin DATETIME NULL,
    ADD COLUMN motivo_cancelacion TEXT NULL,
    ADD COLUMN especializacion_id BIGINT NULL;
UPDATE cita SET estado = UPPER(estado);
UPDATE cita SET estado = 'PENDIENTE' WHERE estado = 'PROGRAMADA';
UPDATE cita SET fecha_hora_fin = DATE_ADD(fecha_hora, INTERVAL 30 MINUTE) WHERE fecha_hora_fin IS NULL;
UPDATE cita c JOIN medico m ON m.id = c.medico_id SET c.especializacion_id = m.especializacion_id WHERE c.especializacion_id IS NULL;
ALTER TABLE cita ADD INDEX idx_cita_medico_fecha (medico_id, fecha_hora);

CREATE TABLE IF NOT EXISTS horario_veterinario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medico_id BIGINT NOT NULL,
    dia_semana TINYINT NOT NULL,          -- 1 = lunes ... 7 = domingo (ISO)
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_horario_medico FOREIGN KEY (medico_id) REFERENCES medico (id) ON DELETE CASCADE
);

-- ==========================================================
-- FASE 3: TARIFAS, ATENCION, FORMULAS, FACTURACION
-- ==========================================================
CREATE TABLE IF NOT EXISTS tarifa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    especializacion_id BIGINT NOT NULL,
    servicio VARCHAR(100) NOT NULL,
    valor DECIMAL(12,2) NOT NULL,
    iva_porcentaje DECIMAL(5,2) NOT NULL DEFAULT 0,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_tarifa_esp FOREIGN KEY (especializacion_id) REFERENCES especializacion (id)
);

CREATE TABLE IF NOT EXISTS tarifa_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tarifa_id BIGINT NOT NULL,
    accion VARCHAR(20) NOT NULL,
    valor_anterior DECIMAL(12,2) NULL,
    valor_nuevo DECIMAL(12,2) NULL,
    iva_anterior DECIMAL(5,2) NULL,
    iva_nuevo DECIMAL(5,2) NULL,
    usuario_id BIGINT NULL,
    fecha DATETIME NOT NULL,
    CONSTRAINT fk_tarifa_log FOREIGN KEY (tarifa_id) REFERENCES tarifa (id)
);

CREATE TABLE IF NOT EXISTS atencion_medica (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cita_id BIGINT NOT NULL UNIQUE,
    medico_id BIGINT NOT NULL,
    tarifa_id BIGINT NULL,
    motivo TEXT NULL,
    sintomas TEXT NULL,
    diagnostico TEXT NULL,
    observaciones TEXT NULL,
    recomendaciones TEXT NULL,
    estado VARCHAR(15) NOT NULL,          -- EN_CURSO, FINALIZADA
    fecha_inicio DATETIME NOT NULL,
    fecha_fin DATETIME NULL,
    CONSTRAINT fk_atencion_cita FOREIGN KEY (cita_id) REFERENCES cita (id),
    CONSTRAINT fk_atencion_medico FOREIGN KEY (medico_id) REFERENCES medico (id)
);

-- La formula pasa a ser cabecera + detalle (varios medicamentos). Se migra la fila antigua.
CREATE TABLE IF NOT EXISTS formula_medica_detalle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    formula_id BIGINT NOT NULL,
    medicamento_id BIGINT NOT NULL,
    dosis VARCHAR(255) NOT NULL,
    frecuencia VARCHAR(100) NOT NULL,
    duracion_dias INT NOT NULL,
    cantidad_prescrita INT NOT NULL,
    via_administracion VARCHAR(50) NULL,
    indicaciones TEXT NULL,
    CONSTRAINT fk_fdet_formula FOREIGN KEY (formula_id) REFERENCES formula_medica (id) ON DELETE CASCADE,
    CONSTRAINT fk_fdet_medicamento FOREIGN KEY (medicamento_id) REFERENCES medicamento (id)
);

INSERT INTO formula_medica_detalle (formula_id, medicamento_id, dosis, frecuencia, duracion_dias, cantidad_prescrita, via_administracion, indicaciones)
SELECT id, medicamento_id, LEFT(dosis, 255), frecuencia, duracion_dias, 1, 'Oral', indicaciones FROM formula_medica WHERE medicamento_id IS NOT NULL;

ALTER TABLE formula_medica DROP FOREIGN KEY fk_formula_medicamento;
ALTER TABLE formula_medica
    DROP COLUMN medicamento_id,
    DROP COLUMN dosis,
    DROP COLUMN frecuencia,
    DROP COLUMN duracion_dias,
    ADD COLUMN medico_id BIGINT NULL,
    ADD COLUMN atencion_id BIGINT NULL,
    ADD COLUMN estado_despacho VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE_DESPACHO',
    ADD COLUMN usuario_id BIGINT NULL;
UPDATE formula_medica f JOIN cita c ON c.id = f.cita_id SET f.medico_id = c.medico_id;

CREATE TABLE IF NOT EXISTS factura (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(20) NULL UNIQUE,
    cita_id BIGINT NOT NULL,
    atencion_id BIGINT NULL,
    cliente_id BIGINT NOT NULL,
    mascota_id INT NOT NULL,
    medico_id BIGINT NOT NULL,
    especializacion_id BIGINT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    impuestos DECIMAL(12,2) NOT NULL,
    descuento DECIMAL(12,2) NOT NULL DEFAULT 0,
    total DECIMAL(12,2) NOT NULL,
    fecha DATETIME NOT NULL,
    estado_pago VARCHAR(15) NOT NULL,     -- PENDIENTE, PARCIAL, PAGADA, ANULADA
    motivo_anulacion VARCHAR(255) NULL,
    fecha_anulacion DATETIME NULL,
    usuario_id BIGINT NULL,
    CONSTRAINT fk_factura_cita FOREIGN KEY (cita_id) REFERENCES cita (id),
    CONSTRAINT fk_factura_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (cliente_id),
    CONSTRAINT fk_factura_mascota FOREIGN KEY (mascota_id) REFERENCES mascota (mascota_id),
    CONSTRAINT fk_factura_medico FOREIGN KEY (medico_id) REFERENCES medico (id)
);

CREATE TABLE IF NOT EXISTS factura_detalle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    factura_id BIGINT NOT NULL,
    tipo VARCHAR(15) NOT NULL,            -- SERVICIO, MEDICAMENTO
    descripcion VARCHAR(255) NOT NULL,
    tarifa_id BIGINT NULL,
    medicamento_id BIGINT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,   -- congelado al facturar
    iva_porcentaje DECIMAL(5,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    iva_valor DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_fdetalle_factura FOREIGN KEY (factura_id) REFERENCES factura (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS pago (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    factura_id BIGINT NOT NULL,
    valor DECIMAL(12,2) NOT NULL,
    metodo VARCHAR(20) NOT NULL,
    referencia VARCHAR(100) NULL,
    fecha DATETIME NOT NULL,
    usuario_id BIGINT NULL,
    CONSTRAINT fk_pago_factura FOREIGN KEY (factura_id) REFERENCES factura (id)
);

-- ==========================================================
-- FASE 4: DESPACHO
-- ==========================================================
CREATE TABLE IF NOT EXISTS despacho (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    formula_id BIGINT NOT NULL UNIQUE,
    estado VARCHAR(30) NOT NULL,
    usuario_id BIGINT NULL,
    fecha_creacion DATETIME NOT NULL,
    fecha_preparacion DATETIME NULL,
    fecha_confirmacion DATETIME NULL,
    observaciones VARCHAR(500) NULL,
    CONSTRAINT fk_despacho_formula FOREIGN KEY (formula_id) REFERENCES formula_medica (id)
);

CREATE TABLE IF NOT EXISTS despacho_detalle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    despacho_id BIGINT NOT NULL,
    formula_detalle_id BIGINT NOT NULL,
    medicamento_id BIGINT NOT NULL,
    cantidad_solicitada INT NOT NULL,
    cantidad_entregada INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_ddet_despacho FOREIGN KEY (despacho_id) REFERENCES despacho (id) ON DELETE CASCADE,
    CONSTRAINT fk_ddet_medicamento FOREIGN KEY (medicamento_id) REFERENCES medicamento (id)
);

CREATE TABLE IF NOT EXISTS despacho_estado_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    despacho_id BIGINT NOT NULL,
    estado_anterior VARCHAR(30) NULL,
    estado_nuevo VARCHAR(30) NOT NULL,
    usuario_id BIGINT NULL,
    fecha DATETIME NOT NULL,
    motivo VARCHAR(500) NULL,
    CONSTRAINT fk_dlog_despacho FOREIGN KEY (despacho_id) REFERENCES despacho (id) ON DELETE CASCADE
);

-- Despacho para las formulas ya existentes
INSERT INTO despacho (formula_id, estado, fecha_creacion) SELECT id, 'PENDIENTE_DESPACHO', NOW() FROM formula_medica;
INSERT INTO despacho_detalle (despacho_id, formula_detalle_id, medicamento_id, cantidad_solicitada)
SELECT d.id, fd.id, fd.medicamento_id, fd.cantidad_prescrita FROM despacho d JOIN formula_medica_detalle fd ON fd.formula_id = d.formula_id;
INSERT INTO despacho_estado_log (despacho_id, estado_anterior, estado_nuevo, fecha, motivo) SELECT id, NULL, 'PENDIENTE_DESPACHO', NOW(), 'Creacion' FROM despacho;
