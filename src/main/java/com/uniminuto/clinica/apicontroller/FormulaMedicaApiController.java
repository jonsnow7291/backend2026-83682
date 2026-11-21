package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.FormulaMedicaService;
import com.uniminuto.clinica.util.Descargas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST de fórmulas médicas.
 */
@RestController
public class FormulaMedicaApiController implements FormulaMedicaApi {

    @Autowired
    private FormulaMedicaService formulaService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<FormulaMedica>> listarFormulasMedicas(Long citaId, Integer mascotaId) {
        return ResponseEntity.ok(formulaService.listarFormulasMedicas(citaId, mascotaId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<FormulaMedica> consultarFormulaMedica(Long formulaId) throws BadRequestException {
        return ResponseEntity.ok(formulaService.consultarFormulaMedica(formulaId));
    }

    /** {@inheritDoc} */
    @Override
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<MiRespuestaRS> guardarFormulaMedica(FormulaMedicaRq rq) throws BadRequestException {
        return ResponseEntity.ok(formulaService.guardarFormulaMedica(rq));
    }

    /** {@inheritDoc} */
    @Override
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<MiRespuestaRS> actualizarFormulaMedica(FormulaMedicaRq rq) throws BadRequestException {
        return ResponseEntity.ok(formulaService.actualizarFormulaMedica(rq));
    }

    /** {@inheritDoc} */
    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<MiRespuestaRS> confirmarFormulaMedica(Long id) throws BadRequestException {
        return ResponseEntity.ok(formulaService.confirmarFormulaMedica(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> disponibilidad(Long id) {
        return ResponseEntity.ok(formulaService.disponibilidad(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<byte[]> pdf(Long id) {
        return Descargas.pdf("formula-" + id + ".pdf", formulaService.pdf(id));
    }
}
