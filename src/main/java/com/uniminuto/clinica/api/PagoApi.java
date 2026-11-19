package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Pago;
import com.uniminuto.clinica.models.FacturaDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrato REST de pagos (ADMIN y SECRETARIA).
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/pago")
@Tag(name = "Pagos", description = "Pagos parciales o totales de facturas")
public interface PagoApi {

    /**
     * @param rq pago.
     * @return factura con el nuevo estado de pago.
     */
    @Operation(summary = "Registrar pago", description = "Parcial o total; recalcula estado_pago (PARCIAL/PAGADA). No puede superar el saldo.")
    @PostMapping(value = "/registrar", consumes = {"application/json"}, produces = {"application/json"})
    ResponseEntity<FacturaDtos.FacturaRs> registrar(@RequestBody FacturaDtos.PagoRq rq);

    /**
     * @param facturaId factura.
     * @return pagos.
     */
    @Operation(summary = "Pagos de una factura")
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<Pago>> listar(@RequestParam Long facturaId);
}
