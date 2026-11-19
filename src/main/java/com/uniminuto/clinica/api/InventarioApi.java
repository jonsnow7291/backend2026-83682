package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.CargaMasiva;
import com.uniminuto.clinica.entity.LoteMedicamento;
import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.models.CargaMasivaRs;
import com.uniminuto.clinica.models.LoteRq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Contrato REST de lotes, movimientos y carga masiva de inventario.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/inventario")
@Tag(name = "Inventario - Lotes y carga masiva", description = "Entradas por lote, historial de movimientos y carga masiva .xlsx/.csv")
public interface InventarioApi {

    /**
     * Registra una entrada de inventario (nuevo lote).
     *
     * @param rq datos del lote.
     * @return lote creado.
     */
    @Operation(summary = "Entrada de inventario (lote)", description = "Genera un movimiento ENTRADA y actualiza el stock. El vencimiento debe ser posterior a la compra.")
    @PostMapping(value = "/entrada", consumes = {"application/json"}, produces = {"application/json"})
    ResponseEntity<LoteMedicamento> entrada(@RequestBody LoteRq rq);

    /**
     * Lista los lotes de un producto.
     *
     * @param medicamentoId producto.
     * @return lotes, el que vence primero al inicio.
     */
    @Operation(summary = "Lotes de un producto")
    @GetMapping(value = "/lotes", produces = {"application/json"})
    ResponseEntity<List<LoteMedicamento>> lotes(@RequestParam Long medicamentoId);

    /**
     * Historial de entradas y salidas.
     *
     * @param medicamentoId filtro opcional por producto.
     * @return movimientos.
     */
    @Operation(summary = "Historial de movimientos", description = "Entradas y salidas, con usuario, fecha, despacho y fórmula asociados.")
    @GetMapping(value = "/movimientos", produces = {"application/json"})
    ResponseEntity<List<MovimientoInventario>> movimientos(@RequestParam(required = false) Long medicamentoId);

    /**
     * Valida un archivo de carga masiva sin guardar nada.
     *
     * @param archivo .xlsx o .csv.
     * @return filas correctas y filas con error (fila + motivo).
     */
    @Operation(summary = "Carga masiva: previsualizar", description = "Valida encabezados, obligatorios, duplicados, precios, cantidades y fechas. NO guarda nada.")
    @ApiResponse(responseCode = "400", description = "Archivo inválido o columnas faltantes")
    @PostMapping(value = "/carga-masiva/previsualizar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = {"application/json"})
    ResponseEntity<CargaMasivaRs> previsualizar(@RequestPart("archivo") MultipartFile archivo);

    /**
     * Importa las filas válidas del archivo.
     *
     * @param archivo .xlsx o .csv.
     * @return resultado con el id de la carga.
     */
    @Operation(summary = "Carga masiva: confirmar", description = "Importa solo las filas válidas, cada una aislada; registra usuario, fecha y log.")
    @PostMapping(value = "/carga-masiva/confirmar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = {"application/json"})
    ResponseEntity<CargaMasivaRs> confirmar(@RequestPart("archivo") MultipartFile archivo);

    /**
     * Historial de cargas masivas.
     *
     * @return cargas realizadas.
     */
    @Operation(summary = "Historial de cargas masivas")
    @GetMapping(value = "/carga-masiva", produces = {"application/json"})
    ResponseEntity<List<CargaMasiva>> historial();

    /**
     * Descarga el informe de errores de una carga.
     *
     * @param id carga.
     * @return archivo .xlsx con las filas rechazadas.
     */
    @Operation(summary = "Informe de errores de una carga (.xlsx)")
    @GetMapping(value = "/carga-masiva/{id}/errores")
    ResponseEntity<byte[]> errores(@PathVariable Long id);

    /**
     * Descarga la plantilla de ejemplo.
     *
     * @param formato xlsx o csv.
     * @return plantilla.
     */
    @Operation(summary = "Plantilla de carga masiva", description = "Descarga un archivo de ejemplo con las columnas esperadas.")
    @GetMapping(value = "/carga-masiva/plantilla")
    ResponseEntity<byte[]> plantilla(@RequestParam(defaultValue = "xlsx") String formato);
}
