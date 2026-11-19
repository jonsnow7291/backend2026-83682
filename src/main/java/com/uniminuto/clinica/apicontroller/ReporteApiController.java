package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.ReporteApi;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ReporteData;
import com.uniminuto.clinica.service.ReporteService;
import com.uniminuto.clinica.util.Descargas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST de reportes; acceso restringido a ADMIN.
 */
@RestController
@PreAuthorize("hasRole('ADMIN')")
public class ReporteApiController implements ReporteApi {

    @Autowired
    private ReporteService reporteService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<String>> catalogo() {
        return ResponseEntity.ok(reporteService.catalogo());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<?> generar(String tipo, LocalDate desde, LocalDate hasta, Long medicoId, Long especializacionId,
                                     Long medicamentoId, String agrupacion, String formato) {
        ReporteData r = reporteService.generar(tipo, desde, hasta, medicoId, especializacionId, medicamentoId, agrupacion);
        return switch (formato.toLowerCase()) {
            case "json" -> ResponseEntity.ok(r);
            case "pdf" -> Descargas.pdf(tipo + ".pdf", reporteService.pdf(r));
            case "xlsx" -> Descargas.xlsx(tipo + ".xlsx", reporteService.xlsx(r));
            default -> throw new BadRequestException("formato debe ser json, pdf o xlsx");
        };
    }
}
