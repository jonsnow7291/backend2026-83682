package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Contrato REST para la consulta de fórmulas médicas (prescripciones) del inventario.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/formula-medica")
public interface FormulaMedicaApi {

    /**
     * Lista todas las fórmulas médicas del inventario, ordenadas de la fecha
     * de creación más reciente a la más antigua.
     *
     * @return respuesta HTTP con la lista completa de fórmulas médicas.
     * @throws BadRequestException si ocurre un error al procesar la solicitud.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<FormulaMedica>> listarFormulasMedicas()
            throws BadRequestException;
}
