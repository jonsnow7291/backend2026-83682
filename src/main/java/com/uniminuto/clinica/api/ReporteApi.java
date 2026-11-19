package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato REST de reportes (solo ADMIN).
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/reportes")
@Tag(name = "Reportes", description = "Reportes financieros, de inventario y de gestión médica en JSON, PDF o Excel (solo ADMIN)")
public interface ReporteApi {

    /**
     * @return códigos de reportes disponibles.
     */
    @Operation(summary = "Catálogo de reportes")
    @GetMapping(value = "/catalogo", produces = {"application/json"})
    ResponseEntity<List<String>> catalogo();

    /**
     * @param tipo          código del reporte.
     * @param desde         inicio del período.
     * @param hasta         fin del período.
     * @param medicoId      veterinario.
     * @param especializacionId especialidad.
     * @param medicamentoId medicamento (historial).
     * @param agrupacion    DIA, MES o ANIO.
     * @param formato       json (previsualización), pdf o xlsx.
     * @return reporte en el formato pedido.
     */
    @Operation(summary = "Generar reporte", description = "formato=json devuelve la previsualización; pdf/xlsx exportan. Incluye encabezado, fecha de generación y totales.")
    @GetMapping(value = "/{tipo}")
    ResponseEntity<?> generar(@PathVariable String tipo,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                              @RequestParam(required = false) Long medicoId,
                              @RequestParam(required = false) Long especializacionId,
                              @RequestParam(required = false) Long medicamentoId,
                              @RequestParam(required = false) String agrupacion,
                              @RequestParam(defaultValue = "json") String formato);
}
