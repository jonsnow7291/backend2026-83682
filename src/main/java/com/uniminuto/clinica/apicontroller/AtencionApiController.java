package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.AtencionApi;
import com.uniminuto.clinica.entity.AtencionMedica;
import com.uniminuto.clinica.models.AtencionRq;
import com.uniminuto.clinica.service.AtencionService;
import com.uniminuto.clinica.util.Descargas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST de atención médica.
 */
@RestController
public class AtencionApiController implements AtencionApi {

    @Autowired
    private AtencionService atencionService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<AtencionMedica> abrir(AtencionRq rq) {
        return ResponseEntity.ok(atencionService.abrir(rq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<AtencionMedica> registrar(AtencionRq rq) {
        return ResponseEntity.ok(atencionService.registrar(rq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<AtencionMedica> finalizar(Long id) {
        return ResponseEntity.ok(atencionService.finalizar(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<AtencionMedica> consultar(Long id) {
        return ResponseEntity.ok(atencionService.consultar(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<AtencionMedica>> historialMascota(Integer mascotaId) {
        return ResponseEntity.ok(atencionService.historialMascota(mascotaId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<byte[]> prefacturaPdf(Long id) {
        return Descargas.pdf("prefactura-" + id + ".pdf", atencionService.prefacturaPdf(id));
    }
}
