package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.DespachoApi;
import com.uniminuto.clinica.entity.DespachoEstadoLog;
import com.uniminuto.clinica.models.DespachoDtos;
import com.uniminuto.clinica.service.DespachoService;
import com.uniminuto.clinica.util.Descargas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST de despacho. Doble barrera de permisos: URL en SecurityConfig y {@code @PreAuthorize}.
 */
@RestController
@PreAuthorize("hasRole('DESPACHO_MEDICO')")
public class DespachoApiController implements DespachoApi {

    @Autowired
    private DespachoService despachoService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<DespachoDtos.Resumen>> pendientes(String q, LocalDate fecha) {
        return ResponseEntity.ok(despachoService.pendientes(q, fecha));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<DespachoDtos.Detalle> detalle(Long id) {
        return ResponseEntity.ok(despachoService.detallePorFormula(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<DespachoDtos.Sugerencia> preparar(Long id) {
        return ResponseEntity.ok(despachoService.preparar(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<DespachoDtos.Sugerencia> sugerencia(Long id) {
        return ResponseEntity.ok(despachoService.sugerir(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<DespachoDtos.Detalle> confirmar(Long id, DespachoDtos.ConfirmarRq rq) {
        return ResponseEntity.ok(despachoService.confirmar(id, rq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<byte[]> comprobante(Long id) {
        return Descargas.pdf("comprobante-despacho-" + id + ".pdf", despachoService.comprobantePdf(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<DespachoDtos.Resumen> cancelar(Long id, DespachoDtos.AutorizacionRq rq) {
        return ResponseEntity.ok(despachoService.cancelar(id, rq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<DespachoDtos.Resumen> reversar(Long id, DespachoDtos.AutorizacionRq rq) {
        return ResponseEntity.ok(despachoService.reversar(id, rq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<DespachoDtos.Resumen>> historial() {
        return ResponseEntity.ok(despachoService.historial());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<DespachoDtos.MovimientoDespacho>> historialMedicamento(Long medicamentoId) {
        return ResponseEntity.ok(despachoService.historialMedicamento(medicamentoId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<DespachoEstadoLog>> estados(Long id) {
        return ResponseEntity.ok(despachoService.estados(id));
    }
}
