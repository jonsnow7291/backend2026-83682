package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementación REST de {@link FormulaMedicaApi}, delega la lógica de negocio
 * en {@link FormulaMedicaService}.
 */
@RestController
public class FormulaMedicaApiController implements FormulaMedicaApi {

    /**
     * Servicio de negocio para operaciones sobre fórmulas médicas.
     */
    @Autowired
    private FormulaMedicaService formulaMedicaService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<FormulaMedica>> listarFormulasMedicas() throws BadRequestException {
        return ResponseEntity.ok(this.formulaMedicaService.listarFormulasMedicas());
    }
}
