package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.PagoApi;
import com.uniminuto.clinica.entity.Pago;
import com.uniminuto.clinica.models.FacturaDtos;
import com.uniminuto.clinica.service.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST de pagos.
 */
@RestController
public class PagoApiController implements PagoApi {

    @Autowired
    private PagoService pagoService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<FacturaDtos.FacturaRs> registrar(FacturaDtos.PagoRq rq) {
        return ResponseEntity.ok(pagoService.registrar(rq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Pago>> listar(Long facturaId) {
        return ResponseEntity.ok(pagoService.listar(facturaId));
    }
}
