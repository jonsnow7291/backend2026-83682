package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FacturaApi;
import com.uniminuto.clinica.models.FacturaDtos;
import com.uniminuto.clinica.service.FacturaService;
import com.uniminuto.clinica.util.Descargas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST de facturación.
 */
@RestController
public class FacturaApiController implements FacturaApi {

    @Autowired
    private FacturaService facturaService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<FacturaDtos.FacturaRs> generar(FacturaDtos.GenerarRq rq) {
        return ResponseEntity.ok(facturaService.generar(rq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<FacturaDtos.FacturaRs> consultar(Long id) {
        return ResponseEntity.ok(facturaService.consultar(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<FacturaDtos.FacturaRs>> listar(Long clienteId, Integer mascotaId) {
        return ResponseEntity.ok(facturaService.listar(clienteId, mascotaId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<FacturaDtos.FacturaRs> anular(Long id, String motivo) {
        return ResponseEntity.ok(facturaService.anular(id, motivo));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<byte[]> pdf(Long id) {
        return Descargas.pdf("factura-" + id + ".pdf", facturaService.pdf(id));
    }
}
