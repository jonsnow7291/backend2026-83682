-- ==========================================================
-- SCRIPT DDL Y DATOS INICIALES - CLÍNICA VETERINARIA
-- ==========================================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS clinica CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE clinica;

-- ----------------------------------------------------------
-- 0. Tablas: usuario y especializacion
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash TEXT NULL,
    rol VARCHAR(30) NOT NULL,
    fecha_creacion DATETIME NULL,
    activo BOOLEAN NULL DEFAULT TRUE,
    email VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS especializacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT NULL,
    codigo_especializacion VARCHAR(10) NOT NULL UNIQUE
);

-- ----------------------------------------------------------
-- 1. Tabla: cliente
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS cliente (
    cliente_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NULL,
    tipo_documento VARCHAR(10) NOT NULL,
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    genero VARCHAR(1) NULL,
    telefono VARCHAR(20) NULL,
    direccion TEXT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- ----------------------------------------------------------
-- 2. Tabla: raza
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS raza (
    raza_id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    especie VARCHAR(100) NOT NULL,
    fecha_creacion DATETIME NOT NULL,
    fecha_modificacion DATETIME NULL
);

-- ----------------------------------------------------------
-- 3. Tabla: mascota
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS mascota (
    mascota_id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_mascota VARCHAR(255) NOT NULL,
    edad INT NOT NULL,
    fecha_registro DATETIME NOT NULL,
    fecha_modificacion DATETIME NULL,
    raza_id INT NULL,
    cliente_id BIGINT NULL,
    CONSTRAINT fk_mascota_raza FOREIGN KEY (raza_id) REFERENCES raza (raza_id) ON DELETE SET NULL,
    CONSTRAINT fk_mascota_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (cliente_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------
-- 4. Tabla: medico
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS medico (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento VARCHAR(10) NOT NULL,
    numero_documento VARCHAR(20) NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NULL,
    registro_profesional VARCHAR(50) NOT NULL,
    especializacion_id INT NOT NULL,
    usuario_id BIGINT NULL,
    CONSTRAINT fk_medico_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE SET NULL
);

-- ----------------------------------------------------------
-- 5. Tabla: medicamento
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS medicamento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT NULL,
    presentacion VARCHAR(100) NULL,
    fecha_compra DATE NOT NULL,
    fecha_vence DATE NOT NULL,
    fecha_creacion_registro DATETIME NOT NULL,
    fecha_modificacion_registro DATETIME NULL
);

-- ----------------------------------------------------------
-- 6. Tabla: cita
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS cita (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    mascota_id INT NOT NULL,
    medico_id BIGINT NOT NULL,
    fecha_hora DATETIME NOT NULL,
    estado VARCHAR(20) NOT NULL,
    motivo TEXT NULL,
    CONSTRAINT fk_cita_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (cliente_id) ON DELETE CASCADE,
    CONSTRAINT fk_cita_mascota FOREIGN KEY (mascota_id) REFERENCES mascota (mascota_id) ON DELETE CASCADE,
    CONSTRAINT fk_cita_medico FOREIGN KEY (medico_id) REFERENCES medico (id) ON DELETE CASCADE
);

-- ----------------------------------------------------------
-- 7. Tabla: historia_medica
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS historia_medica (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paciente_id INT NOT NULL,
    cita_id BIGINT NULL,
    diagnostico TEXT NULL,
    observaciones TEXT NULL,
    fecha_creacion DATETIME NULL,
    CONSTRAINT fk_historia_mascota FOREIGN KEY (paciente_id) REFERENCES mascota (mascota_id) ON DELETE CASCADE,
    CONSTRAINT fk_historia_cita FOREIGN KEY (cita_id) REFERENCES cita (id)
);

-- ----------------------------------------------------------
-- 8. Tabla: anotacion_historia
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS anotacion_historia (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    historia_id BIGINT NOT NULL,
    medico_id BIGINT NOT NULL,
    fecha DATETIME NULL,
    descripcion TEXT NOT NULL,
    observacion TEXT NULL,
    diagnostico TEXT NULL,
    CONSTRAINT fk_anotacion_historia FOREIGN KEY (historia_id) REFERENCES historia_medica (id) ON DELETE CASCADE,
    CONSTRAINT fk_anotacion_medico FOREIGN KEY (medico_id) REFERENCES medico (id) ON DELETE CASCADE
);

-- ----------------------------------------------------------
-- 9. Tabla: formula_medica
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS formula_medica (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cita_id BIGINT NOT NULL,
    medicamento_id BIGINT NOT NULL,
    dosis TEXT NOT NULL,
    frecuencia VARCHAR(100) NOT NULL,
    duracion_dias INT NOT NULL,
    indicaciones TEXT NULL,
    fecha_emision DATETIME NOT NULL,
    fecha_creacion_registro DATETIME NOT NULL,
    fecha_actualizacion_registro DATETIME NULL,
    CONSTRAINT fk_formula_cita FOREIGN KEY (cita_id) REFERENCES cita (id) ON DELETE CASCADE,
    CONSTRAINT fk_formula_medicamento FOREIGN KEY (medicamento_id) REFERENCES medicamento (id) ON DELETE CASCADE
);

-- ==========================================================
-- DATOS INICIALES DE PRUEBA (Para pruebas en Postman)
-- ==========================================================

INSERT INTO cliente (cliente_id, usuario_id, tipo_documento, numero_documento, nombres, apellidos, fecha_nacimiento, genero, telefono, direccion, activo)
VALUES 
(1, 1, 'CC', '1000000001', 'Juan', 'Pérez', '1990-05-15', 'M', '3001234567', 'Calle 123 #45-67', TRUE),
(2, 2, 'CC', '1000000002', 'Ana', 'Gómez', '1995-08-20', 'F', '3159988776', 'Carrera 7 #45-12', TRUE)
ON DUPLICATE KEY UPDATE numero_documento=numero_documento;

INSERT INTO raza (raza_id, nombre, especie, fecha_creacion)
VALUES (1, 'Golden Retriever', 'Canino', NOW())
ON DUPLICATE KEY UPDATE nombre=nombre;

INSERT INTO mascota (mascota_id, nombre_mascota, edad, fecha_registro, raza_id, cliente_id)
VALUES 
(1, 'Firulais', 2, NOW(), 1, 1),
(2, 'Luna', 1, NOW(), 1, 1),
(3, 'Michi', 3, NOW(), 1, 2)
ON DUPLICATE KEY UPDATE nombre_mascota=nombre_mascota;

INSERT INTO medico (id, tipo_documento, numero_documento, nombres, apellidos, telefono, registro_profesional, especializacion_id)
VALUES 
(1, 'CC', '900000001', 'Dra. María', 'González', '3109876543', 'VET-001', 1),
(2, 'CC', '900000002', 'Dr. Carlos', 'Rodríguez', '3118765432', 'VET-002', 2),
(3, 'CC', '900000003', 'Dra. Laura', 'Martínez', '3201122334', 'VET-003', 1)
ON DUPLICATE KEY UPDATE numero_documento=numero_documento;

INSERT INTO medicamento (id, nombre, descripcion, presentacion, fecha_compra, fecha_vence, fecha_creacion_registro)
VALUES (1, 'Amoxicilina 250mg', 'Antibiótico de amplio espectro', 'Frasco 60ml suspensión', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 YEAR), NOW())
ON DUPLICATE KEY UPDATE nombre=nombre;

-- Citas ya atendidas (en el pasado) para probar historias clínicas y fórmulas médicas.
-- La API no permite crear citas en fechas pasadas, por eso se cargan aquí.
INSERT INTO cita (id, cliente_id, mascota_id, medico_id, fecha_hora, estado, motivo)
VALUES
(1, 1, 1, 1, NOW() - INTERVAL 2 DAY, 'Atendida', 'Control de vacunas'),
(2, 1, 2, 2, NOW() - INTERVAL 1 DAY, 'Atendida', 'Dolor de oído'),
(3, 2, 3, 3, NOW() - INTERVAL 3 HOUR, 'Atendida', 'Revisión general')
ON DUPLICATE KEY UPDATE motivo=motivo;

-- Usuarios de prueba por rol. Contraseña de todos los que pueden iniciar sesión: Clinica2026*
-- (hash BCrypt). Los usuarios CLIENTE no tienen acceso al sistema (sin contraseña).
INSERT INTO usuario (id, username, password_hash, rol, fecha_creacion, activo, email)
VALUES
(1, 'jperez', NULL, 'CLIENTE', NOW(), TRUE, 'juan.perez@mail.com'),
(2, 'agomez', NULL, 'CLIENTE', NOW(), TRUE, 'ana.gomez@mail.com'),
(3, 'admin', '$2a$10$gIOou07.Z4ivD4wTkukhSOjckjj/kD8O8P0fnAn5it2PuJnJgM2QC', 'ADMIN', NOW(), TRUE, 'admin@clinica.com'),
(4, 'secretaria', '$2a$10$gIOou07.Z4ivD4wTkukhSOjckjj/kD8O8P0fnAn5it2PuJnJgM2QC', 'SECRETARIA', NOW(), TRUE, 'secretaria@clinica.com'),
(5, 'medico1', '$2a$10$gIOou07.Z4ivD4wTkukhSOjckjj/kD8O8P0fnAn5it2PuJnJgM2QC', 'MEDICO', NOW(), TRUE, 'maria.gonzalez@clinica.com'),
(6, 'medico2', '$2a$10$gIOou07.Z4ivD4wTkukhSOjckjj/kD8O8P0fnAn5it2PuJnJgM2QC', 'MEDICO', NOW(), TRUE, 'carlos.rodriguez@clinica.com'),
(7, 'despacho', '$2a$10$gIOou07.Z4ivD4wTkukhSOjckjj/kD8O8P0fnAn5it2PuJnJgM2QC', 'DESPACHO_MEDICO', NOW(), TRUE, 'despacho@clinica.com')
ON DUPLICATE KEY UPDATE username=username;

-- Vínculo médico <-> usuario (el médico solo ve sus propias citas)
UPDATE medico SET usuario_id = 5 WHERE id = 1;
UPDATE medico SET usuario_id = 6 WHERE id = 2;

INSERT INTO especializacion (id, nombre, descripcion, codigo_especializacion)
VALUES
(1, 'Medicina General', 'Atención primaria de pequeños animales', 'MG'),
(2, 'Cirugía', 'Cirugía veterinaria de tejidos blandos y ortopedia', 'CIR')
ON DUPLICATE KEY UPDATE nombre=nombre;

INSERT INTO historia_medica (id, paciente_id, cita_id, diagnostico, observaciones, fecha_creacion)
VALUES
(1, 1, 1, 'Paciente sano', 'Esquema de vacunación al día', NOW())
ON DUPLICATE KEY UPDATE paciente_id=paciente_id;

INSERT INTO anotacion_historia (id, historia_id, medico_id, fecha, descripcion, observacion, diagnostico)
VALUES
(1, 1, 1, NOW(), 'Control de vacunas', 'Sin reacciones adversas', 'Sano')
ON DUPLICATE KEY UPDATE descripcion=descripcion;

INSERT INTO formula_medica (id, cita_id, medicamento_id, dosis, frecuencia, duracion_dias, indicaciones, fecha_emision, fecha_creacion_registro)
VALUES
(1, 2, 1, '5 ml', 'Cada 12 horas', 7, 'Administrar con comida', NOW(), NOW())
ON DUPLICATE KEY UPDATE dosis=dosis;

-- ==========================================================
-- FASES 1 A 5 (equivale a sql/V02 y sql/V03)
-- ==========================================================
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
-- V03: datos de prueba de las Fases 1 a 5 (inventario, horarios, tarifas, citas futuras).

UPDATE medicamento SET precio_compra = 8000, precio_venta = 12000, stock_minimo = 10, iva_porcentaje = 0 WHERE id = 1;

INSERT INTO medicamento (id, codigo, nombre, descripcion, presentacion, tipo, registro_invima, precio_compra, precio_venta, iva_porcentaje, stock_minimo, fecha_ingreso, fecha_proximo_pedido, activo, fecha_creacion_registro)
VALUES
(2, 'MED00002', 'Meloxicam 1.5mg', 'Antiinflamatorio', 'Frasco 10ml', 'MEDICAMENTO', 'INVIMA-2024-002', 15000, 24000, 0, 5, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), TRUE, NOW()),
(3, 'MED00003', 'Ivermectina 1%', 'Antiparasitario', 'Frasco 50ml', 'MEDICAMENTO', 'INVIMA-2024-003', 20000, 32000, 19, 5, CURDATE(), NULL, TRUE, NOW()),
(4, 'UTL00001', 'Jeringa 5ml', 'Jeringa desechable', 'Caja x100', 'UTENSILIO', NULL, 9000, 15000, 19, 3, CURDATE(), NULL, TRUE, NOW()),
(5, 'MED00005', 'Dipirona 500mg', 'Analgesico (sin existencias)', 'Caja x20', 'MEDICAMENTO', 'INVIMA-2024-005', 6000, 10000, 0, 5, CURDATE(), NULL, TRUE, NOW())
ON DUPLICATE KEY UPDATE nombre = nombre;

INSERT INTO lote_medicamento (id, medicamento_id, numero_lote, cantidad_inicial, cantidad_disponible, precio_compra, fecha_compra, fecha_vencimiento, fecha_creacion)
VALUES
(1, 1, 'L-AMX-001', 40, 40, 8000, DATE_SUB(CURDATE(), INTERVAL 60 DAY), DATE_ADD(CURDATE(), INTERVAL 20 DAY), NOW()),
(2, 1, 'L-AMX-002', 60, 60, 8000, DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 300 DAY), NOW()),
(3, 2, 'L-MEL-001', 30, 30, 15000, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 400 DAY), NOW()),
(4, 3, 'L-IVE-001', 3, 3, 20000, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 200 DAY), NOW()),
(5, 3, 'L-IVE-OLD', 10, 10, 20000, DATE_SUB(CURDATE(), INTERVAL 500 DAY), DATE_SUB(CURDATE(), INTERVAL 5 DAY), NOW()),
(6, 4, 'L-JER-001', 20, 20, 9000, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 700 DAY), NOW())
ON DUPLICATE KEY UPDATE numero_lote = numero_lote;

INSERT INTO movimiento_inventario (medicamento_id, lote_id, tipo, cantidad, fecha, usuario_id, motivo)
SELECT medicamento_id, id, 'ENTRADA', cantidad_inicial, NOW(), 3, 'Inventario inicial' FROM lote_medicamento;

-- stock_actual = suma de lotes VIGENTES
UPDATE medicamento m SET stock_actual = COALESCE((SELECT SUM(l.cantidad_disponible) FROM lote_medicamento l
    WHERE l.medicamento_id = m.id AND l.fecha_vencimiento >= CURDATE()), 0);

-- Horario lunes a sabado 08:00-12:00 y 14:00-18:00 para los 3 medicos
INSERT INTO horario_veterinario (medico_id, dia_semana, hora_inicio, hora_fin, activo)
SELECT m.id, d.dia, h.ini, h.fin, TRUE
FROM medico m
JOIN (SELECT 1 dia UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6) d
JOIN (SELECT '08:00:00' ini, '12:00:00' fin UNION SELECT '14:00:00', '18:00:00') h;

INSERT INTO tarifa (id, especializacion_id, servicio, valor, iva_porcentaje, activa) VALUES
(1, 1, 'Consulta general', 60000, 0, TRUE),
(2, 1, 'Vacunacion', 45000, 0, TRUE),
(3, 2, 'Consulta de cirugia', 90000, 0, TRUE),
(4, 2, 'Procedimiento quirurgico menor', 250000, 19, TRUE)
ON DUPLICATE KEY UPDATE servicio = servicio;

-- Citas atendidas de prueba: la 2 tiene formula con despacho pendiente
INSERT INTO atencion_medica (cita_id, medico_id, tarifa_id, motivo, sintomas, diagnostico, observaciones, recomendaciones, estado, fecha_inicio, fecha_fin)
SELECT c.id, c.medico_id, CASE c.medico_id WHEN 2 THEN 3 ELSE 1 END, c.motivo, 'N/A', 'Seed', 'Atencion de prueba', 'Control en 15 dias', 'FINALIZADA', c.fecha_hora, c.fecha_hora_fin
FROM cita c WHERE c.id IN (2);
UPDATE cita SET estado = 'FINALIZADA' WHERE id = 2;
UPDATE formula_medica SET atencion_id = (SELECT id FROM atencion_medica WHERE cita_id = 2), estado_despacho = 'PENDIENTE_DESPACHO' WHERE id = 1;
UPDATE formula_medica_detalle SET cantidad_prescrita = 2 WHERE formula_id = 1;
UPDATE despacho_detalle SET cantidad_solicitada = 2 WHERE despacho_id IN (SELECT id FROM despacho WHERE formula_id = 1);

-- ============================================================
-- V04: alineacion con el enunciado (ver sql/V04__alineacion_enunciado.sql)
-- ============================================================
ALTER TABLE lote_medicamento MODIFY fecha_vencimiento DATE NULL;

ALTER TABLE formula_medica
    ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    ADD COLUMN fecha_confirmacion DATETIME NULL;
-- Las formulas existentes ya tienen despacho: se consideran confirmadas.
UPDATE formula_medica SET estado = 'CONFIRMADA', fecha_confirmacion = fecha_emision;

ALTER TABLE formula_medica_detalle
    ADD COLUMN unidades_por_dosis INT NULL,
    ADD COLUMN dosis_por_dia INT NULL;
