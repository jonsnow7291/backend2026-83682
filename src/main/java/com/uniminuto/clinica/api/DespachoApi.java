package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.DespachoEstadoLog;
import com.uniminuto.clinica.models.DespachoDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato REST del despacho de medicamentos. Exclusivo del perfil DESPACHO_MEDICO (403 para los demás).
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/despacho")
@Tag(name = "Despacho y dispensación", description = "Perfil DESPACHO_MEDICO: preparación FEFO, confirmación transaccional, comprobante y reversión autorizada")
public interface DespachoApi {

    /**
     * @param q     id de fórmula, propietario o mascota.
     * @param fecha fecha de emisión.
     * @return despachos pendientes.
     */
    @Operation(summary = "Despachos pendientes", description = "Búsqueda por id de fórmula, propietario, mascota o fecha.")
    @GetMapping(value = "/pendientes", produces = {"application/json"})
    ResponseEntity<List<DespachoDtos.Resumen>> pendientes(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha);

    /**
     * @param id fórmula.
     * @return detalle con existencias y lotes.
     */
    @Operation(summary = "Detalle de la fórmula a despachar", description = "Existencias, lotes disponibles, vencidos y próximos a vencer.")
    @GetMapping(value = "/formula/{id}", produces = {"application/json"})
    ResponseEntity<DespachoDtos.Detalle> detalle(@PathVariable Long id);

    /**
     * @param id despacho.
     * @return sugerencia FEFO.
     */
    @Operation(summary = "Preparar despacho", description = "Pasa a EN_PREPARACION y sugiere lotes FEFO (primero el que vence antes, solo vigentes).")
    @PostMapping(value = "/{id}/preparar", produces = {"application/json"})
    ResponseEntity<DespachoDtos.Sugerencia> preparar(@PathVariable Long id);

    /**
     * @param id despacho.
     * @return sugerencia FEFO.
     */
    @Operation(summary = "Sugerencia de lotes FEFO")
    @GetMapping(value = "/{id}/sugerencia-lotes", produces = {"application/json"})
    ResponseEntity<DespachoDtos.Sugerencia> sugerencia(@PathVariable Long id);

    /**
     * @param id despacho.
     * @param rq cantidades (opcional).
     * @return detalle actualizado.
     */
    @Operation(summary = "Confirmar despacho", description = "Transaccional: valida existencias y vencimientos, descuenta del lote, crea movimientos SALIDA ligados al despacho y la fórmula. No permite doble descuento (409).")
    @ApiResponse(responseCode = "409", description = "El despacho ya fue confirmado o cancelado")
    @PostMapping(value = "/{id}/confirmar", produces = {"application/json"})
    ResponseEntity<DespachoDtos.Detalle> confirmar(@PathVariable Long id, @RequestBody(required = false) DespachoDtos.ConfirmarRq rq);

    /**
     * @param id despacho.
     * @return PDF.
     */
    @Operation(summary = "Comprobante de despacho en PDF")
    @GetMapping(value = "/{id}/comprobante-pdf", produces = {"application/pdf"})
    ResponseEntity<byte[]> comprobante(@PathVariable Long id);

    /**
     * @param id despacho.
     * @param rq motivo y autorización de un ADMIN.
     * @return resumen.
     */
    @Operation(summary = "Cancelar despacho sin entregas", description = "Requiere motivo y credenciales de un ADMIN. Deja traza; no se elimina.")
    @PostMapping(value = "/{id}/cancelar", produces = {"application/json"})
    ResponseEntity<DespachoDtos.Resumen> cancelar(@PathVariable Long id, @RequestBody DespachoDtos.AutorizacionRq rq);

    /**
     * @param id despacho.
     * @param rq motivo y autorización de un ADMIN.
     * @return resumen.
     */
    @Operation(summary = "Reversar despacho confirmado", description = "Devuelve las unidades a sus lotes con movimientos ENTRADA. Requiere autorización de un ADMIN.")
    @PostMapping(value = "/{id}/reversar", produces = {"application/json"})
    ResponseEntity<DespachoDtos.Resumen> reversar(@PathVariable Long id, @RequestBody DespachoDtos.AutorizacionRq rq);

    /**
     * @return historial de despachos.
     */
    @Operation(summary = "Historial de despachos")
    @GetMapping(value = "/historial", produces = {"application/json"})
    ResponseEntity<List<DespachoDtos.Resumen>> historial();

    /**
     * @param medicamentoId medicamento.
     * @return movimientos de despacho del medicamento.
     */
    @Operation(summary = "Historial de despachos por medicamento")
    @GetMapping(value = "/historial-medicamento", produces = {"application/json"})
    ResponseEntity<List<DespachoDtos.MovimientoDespacho>> historialMedicamento(@RequestParam Long medicamentoId);

    /**
     * @param id despacho.
     * @return cambios de estado con usuario y fecha.
     */
    @Operation(summary = "Historial de estados de un despacho")
    @GetMapping(value = "/{id}/estados", produces = {"application/json"})
    ResponseEntity<List<DespachoEstadoLog>> estados(@PathVariable Long id);
}
