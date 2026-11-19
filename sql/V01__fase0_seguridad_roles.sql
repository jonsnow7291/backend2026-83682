-- V01: Fase 0 - seguridad y roles
-- Para bases ya creadas (en una BD nueva, schema.sql ya incluye todo esto).
-- Roles validos en usuario.rol: ADMIN, SECRETARIA, MEDICO, DESPACHO_MEDICO (CLIENTE sin acceso).
-- Contraseña de prueba de todos los usuarios con acceso: Clinica2026*

ALTER TABLE medico ADD COLUMN usuario_id BIGINT NULL;
ALTER TABLE medico ADD CONSTRAINT fk_medico_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE SET NULL;

UPDATE usuario SET password_hash = '$2a$10$gIOou07.Z4ivD4wTkukhSOjckjj/kD8O8P0fnAn5it2PuJnJgM2QC' WHERE username = 'admin';

INSERT INTO usuario (id, username, password_hash, rol, fecha_creacion, activo, email) VALUES
(4, 'secretaria', '$2a$10$gIOou07.Z4ivD4wTkukhSOjckjj/kD8O8P0fnAn5it2PuJnJgM2QC', 'SECRETARIA', NOW(), TRUE, 'secretaria@clinica.com'),
(5, 'medico1', '$2a$10$gIOou07.Z4ivD4wTkukhSOjckjj/kD8O8P0fnAn5it2PuJnJgM2QC', 'MEDICO', NOW(), TRUE, 'maria.gonzalez@clinica.com'),
(6, 'medico2', '$2a$10$gIOou07.Z4ivD4wTkukhSOjckjj/kD8O8P0fnAn5it2PuJnJgM2QC', 'MEDICO', NOW(), TRUE, 'carlos.rodriguez@clinica.com'),
(7, 'despacho', '$2a$10$gIOou07.Z4ivD4wTkukhSOjckjj/kD8O8P0fnAn5it2PuJnJgM2QC', 'DESPACHO_MEDICO', NOW(), TRUE, 'despacho@clinica.com')
ON DUPLICATE KEY UPDATE username = username;

UPDATE medico SET usuario_id = 5 WHERE id = 1;
UPDATE medico SET usuario_id = 6 WHERE id = 2;
