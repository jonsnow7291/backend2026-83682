package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.AnotacionHistoriaApi;
import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementación REST de {@link AnotacionHistoriaApi}, delega la lógica de negocio
 * en {@link AnotacionHistoriaService}.
 */
@RestController
public class AnotacionHistoriaApiController implements AnotacionHistoriaApi {

    /**
     * Servicio de negocio para operaciones sobre anotaciones de historia médica.
     */
    @Autowired
    private AnotacionHistoriaService anotacionHistoriaService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<AnotacionHistoria>> listarAnotaciones(Long historiaId) throws BadRequestException {
        return ResponseEntity.ok(this.anotacionHistoriaService.listarAnotaciones(historiaId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> guardarAnotacionHistoria(AnotacionHistoriaRq anotacionHistoriaRq)
            throws BadRequestException {
        return ResponseEntity.ok(this.anotacionHistoriaService.guardarAnotacionHistoria(anotacionHistoriaRq));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> actualizarAnotacionHistoria(AnotacionHistoriaRq anotacionHistoriaRq)
            throws BadRequestException {
        return ResponseEntity.ok(this.anotacionHistoriaService.actualizarAnotacionHistoria(anotacionHistoriaRq));
    }
}
