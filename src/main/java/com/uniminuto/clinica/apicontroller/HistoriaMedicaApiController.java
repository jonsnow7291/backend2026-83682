package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.HistoriaMedicaApi;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.HistoriaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación REST de {@link HistoriaMedicaApi}, delega la lógica de negocio
 * en {@link HistoriaMedicaService}.
 */
@RestController
public class HistoriaMedicaApiController implements HistoriaMedicaApi {

    /**
     * Servicio de negocio para operaciones sobre historias médicas.
     */
    @Autowired
    private HistoriaMedicaService historiaMedicaService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<HistoriaMedica>> listarHistoriasMedicas(
            LocalDateTime fechaInicial, LocalDateTime fechaFinal) throws BadRequestException {
        return ResponseEntity.ok(this.historiaMedicaService.listarHistoriasMedicas(fechaInicial, fechaFinal));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> guardarHistoriaMedica(HistoriaMedicaRq historiaMedicaRq)
            throws BadRequestException {
        return ResponseEntity.ok(this.historiaMedicaService.guardarHistoriaMedica(historiaMedicaRq));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> actualizarHistoriaMedica(HistoriaMedicaRq historiaMedicaRq)
            throws BadRequestException {
        return ResponseEntity.ok(this.historiaMedicaService.actualizarHistoriaMedica(historiaMedicaRq));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> eliminarHistoriaMedica(Long historiaId) throws BadRequestException {
        return ResponseEntity.ok(this.historiaMedicaService.eliminarHistoriaMedica(historiaId));
    }
}
