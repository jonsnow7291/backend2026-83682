package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.CitaApi;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaCalendarioRs;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.ReprogramarRq;
import com.uniminuto.clinica.service.CitaService;
import com.uniminuto.clinica.util.Descargas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST de citas; delega en {@link CitaService}.
 */
@RestController
public class CitaApiController implements CitaApi {

    @Autowired
    private CitaService citaService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Cita>> listarCitasPorFecha(LocalDateTime fechaInicial, LocalDateTime fechaFinal) throws BadRequestException {
        return ResponseEntity.ok(citaService.listarCitasPorFecha(fechaInicial, fechaFinal));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Cita> consultarCita(Long citaId) throws BadRequestException {
        return ResponseEntity.ok(citaService.consultarCita(citaId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Cita>> listarCitasAtendidasPorMascota(Integer mascotaId) throws BadRequestException {
        return ResponseEntity.ok(citaService.listarCitasAtendidasPorMascota(mascotaId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> guardarCita(CitaRq citaRq) throws BadRequestException {
        return ResponseEntity.ok(citaService.guardarCita(citaRq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> actualizarCita(CitaRq citaRq) throws BadRequestException {
        return ResponseEntity.ok(citaService.actualizarCita(citaRq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> cancelarCita(Long citaId, String motivo) throws BadRequestException {
        return ResponseEntity.ok(citaService.cancelarCita(citaId, motivo));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> reprogramar(ReprogramarRq rq) {
        return ResponseEntity.ok(citaService.reprogramar(rq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> confirmar(Long citaId) {
        return ResponseEntity.ok(citaService.confirmar(citaId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> atender(Long citaId) {
        return ResponseEntity.ok(citaService.marcarAtendida(citaId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<CitaCalendarioRs>> calendario(Long medicoId, LocalDateTime desde, LocalDateTime hasta) {
        return ResponseEntity.ok(citaService.calendario(medicoId, desde, hasta));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Cita>> historialMascota(Integer mascotaId) {
        return ResponseEntity.ok(citaService.historialMascota(mascotaId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Long>> resumen(Long medicoId, LocalDateTime desde, LocalDateTime hasta) {
        return ResponseEntity.ok(citaService.resumenPorEstado(medicoId, desde, hasta));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<byte[]> reporteExcel(Long medicoId, LocalDateTime desde, LocalDateTime hasta) {
        return Descargas.xlsx("citas.xlsx", citaService.reporteExcel(medicoId, desde, hasta));
    }
}
