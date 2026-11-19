package com.uniminuto.clinica.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Contenido de un reporte: encabezado, filas y totales. Sirve de JSON (previsualización) y de
 * origen para exportar a PDF o Excel.
 *
 * @param codigo   código del reporte.
 * @param titulo   título.
 * @param desde    inicio del período (puede ser nulo en reportes de estado actual).
 * @param hasta    fin del período.
 * @param generado fecha de generación.
 * @param columnas encabezados.
 * @param filas    filas de datos.
 * @param totales  totales y resúmenes.
 */
public record ReporteData(String codigo, String titulo, LocalDate desde, LocalDate hasta, LocalDateTime generado,
                          List<String> columnas, List<List<Object>> filas, Map<String, Object> totales) {
}
