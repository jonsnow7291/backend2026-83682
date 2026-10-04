package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.EspecializacionApi;
import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.service.EspecializacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST que expone la consulta de especializaciones médicas.
 */
@RestController
public class EspecializacionApiController implements EspecializacionApi {

    /**
     * Servicio con la lógica de negocio de especializaciones médicas.
     */
    @Autowired
    private EspecializacionService especializacionService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<Especializacion>> listarEspecializaciones() throws BadRequestException {
        return ResponseEntity.ok(this.especializacionService.obtenerEspecializaciones());
    }
}
