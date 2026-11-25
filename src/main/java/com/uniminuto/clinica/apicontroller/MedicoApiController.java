package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.MedicoApi;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.service.MedicoService;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Medico> guardarMedico(MedicoRq rq) {
        return ResponseEntity.ok(medicoService.guardarMedico(rq));
    }

    @Override
    public ResponseEntity<Medico> actualizarMedico(MedicoRq rq) {
        return ResponseEntity.ok(medicoService.actualizarMedico(rq));
    }

    @Override
    public ResponseEntity<Medico> cambiarEstado(Long id, boolean activo) {
        return ResponseEntity.ok(medicoService.cambiarEstado(id, activo));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> crearAccesoUsuario(Long id) throws BadRequestException {
        return ResponseEntity.ok(medicoService.crearAccesoUsuario(id));
    }
}
