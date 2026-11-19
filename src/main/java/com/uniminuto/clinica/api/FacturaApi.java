package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.FacturaDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrato REST de facturación (ADMIN y SECRETARIA).
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/factura")
@Tag(name = "Facturación", description = "Facturas de citas atendidas, anulación controlada y PDF")
public interface FacturaApi {

    /**
     * @param rq cita y descuento opcional.
     * @return factura generada.
     */
    @Operation(summary = "Generar factura de una cita atendida", description = "Incluye el servicio (tarifa congelada) y solo los medicamentos efectivamente despachados.")
    @ApiResponse(responseCode = "409", description = "La cita ya tiene factura vigente")
    @PostMapping(value = "/generar", consumes = {"application/json"}, produces = {"application/json"})
    ResponseEntity<FacturaDtos.FacturaRs> generar(@RequestBody FacturaDtos.GenerarRq rq);

    /**
     * @param id factura.
     * @return factura.
     */
    @Operation(summary = "Consultar factura")
    @GetMapping(value = "/consultar", produces = {"application/json"})
    ResponseEntity<FacturaDtos.FacturaRs> consultar(@RequestParam Long id);

    /**
     * @param clienteId propietario (opcional).
     * @param mascotaId mascota (opcional).
     * @return historial de facturación.
     */
    @Operation(summary = "Historial de facturación por propietario o mascota")
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<FacturaDtos.FacturaRs>> listar(@RequestParam(required = false) Long clienteId,
                                                       @RequestParam(required = false) Integer mascotaId);

    /**
     * @param id     factura.
     * @param motivo motivo.
     * @return factura anulada.
     */
    @Operation(summary = "Anular factura", description = "Procedimiento controlado: exige motivo, no admite pagos registrados y conserva el histórico (nunca se borra).")
    @PatchMapping(value = "/anular", produces = {"application/json"})
    ResponseEntity<FacturaDtos.FacturaRs> anular(@RequestParam Long id, @RequestParam String motivo);

    /**
     * @param id factura.
     * @return PDF.
     */
    @Operation(summary = "Factura en PDF")
    @GetMapping(value = "/{id}/pdf", produces = {"application/pdf"})
    ResponseEntity<byte[]> pdf(@PathVariable Long id);
}
