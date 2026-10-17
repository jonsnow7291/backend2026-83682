-- ==========================================================
-- SCRIPT DDL Y DATOS INICIALES - CLÍNICA VETERINARIA
-- ==========================================================

CREATE DATABASE IF NOT EXISTS clinica CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE clinica;

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
    especializacion_id INT NOT NULL
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
    fecha_creacion DATETIME NULL,
    CONSTRAINT fk_historia_mascota FOREIGN KEY (paciente_id) REFERENCES mascota (mascota_id) ON DELETE CASCADE
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
