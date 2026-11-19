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
