package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.HorarioVeterinarioApi;
import com.uniminuto.clinica.entity.HorarioVeterinario;
import com.uniminuto.clinica.models.HorarioRq;
import com.uniminuto.clinica.service.HorarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST de horarios de veterinarios.
 */
@RestController
public class HorarioVeterinarioApiController implements HorarioVeterinarioApi {

    @Autowired
    private HorarioService horarioService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<HorarioVeterinario>> listar(Long medicoId) {
        return ResponseEntity.ok(horarioService.listar(medicoId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<HorarioVeterinario> crear(HorarioRq rq) {
        return ResponseEntity.ok(horarioService.crear(rq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<HorarioVeterinario> actualizar(HorarioRq rq) {
        return ResponseEntity.ok(horarioService.actualizar(rq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Void> eliminar(Long id) {
        horarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
