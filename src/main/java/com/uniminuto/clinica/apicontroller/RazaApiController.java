package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.RazaApi;
import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.service.RazaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST que expone las operaciones de razas.
 */
@RestController
public class RazaApiController implements RazaApi {

    /**
     * Servicio con la lógica de negocio de razas.
     */
    @Autowired
    private RazaService razaService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<Raza>> listarRazas() throws BadRequestException {
        return ResponseEntity.ok(this.razaService.obtenerRazas());
    }
}
