package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.ReporteData;

import java.time.LocalDate;
import java.util.List;

/**
 * Reportes financieros, de inventario y de gestión médica.
 */
public interface ReporteService {

    /**
     * @return códigos de reportes disponibles con su descripción.
     */
    List<String> catalogo();

    /**
     * Genera un reporte.
     *
     * @param tipo            código del reporte (ver {@link #catalogo()}).
     * @param desde           inicio del período (por defecto el primer día del mes).
     * @param hasta           fin del período (por defecto hoy).
     * @param medicoId        filtro opcional por veterinario.
     * @param especializacion filtro opcional por especialidad.
     * @param medicamentoId   filtro opcional/obligatorio según el reporte.
     * @param agrupacion      DIA, MES o ANIO (solo resumen financiero).
     * @return datos del reporte.
     */
    ReporteData generar(String tipo, LocalDate desde, LocalDate hasta, Long medicoId, Long especializacion,
                        Long medicamentoId, String agrupacion);

    /**
     * @param r datos.
     * @return PDF del reporte.
     */
    byte[] pdf(ReporteData r);

    /**
     * @param r datos.
     * @return Excel del reporte.
     */
    byte[] xlsx(ReporteData r);
}
