package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.MedicoApi;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.service.MedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST que expone la consulta de médicos veterinarios.
 */
@RestController
public class MedicoApiController implements MedicoApi {

    /**
     * Servicio con la lógica de negocio de médicos veterinarios.
     */
    @Autowired
    private MedicoService medicoService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<Medico>> listarMedicos() throws BadRequestException {
        return ResponseEntity.ok(this.medicoService.obtenerMedicos());
    }
}
