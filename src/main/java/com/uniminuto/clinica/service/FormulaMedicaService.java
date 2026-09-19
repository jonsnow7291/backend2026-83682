package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;

import java.util.List;

/**
 * Servicio de negocio para la consulta de fórmulas médicas (prescripciones) del inventario.
 */
public interface FormulaMedicaService {

    /**
     * Lista todas las fórmulas médicas registradas, ordenadas de la más reciente
     * a la más antigua según su fecha de creación.
     *
     * @return lista completa de fórmulas médicas.
     * @throws BadRequestException si ocurre un error al procesar la solicitud.
     */
    List<FormulaMedica> listarFormulasMedicas() throws BadRequestException;
}
