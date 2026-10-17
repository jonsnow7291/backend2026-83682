package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

/**
 * Servicio de negocio para la gestión de fórmulas médicas (prescripciones).
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

    /**
     * Consulta una fórmula médica específica por su identificador.
     *
     * @param formulaId identificador de la fórmula médica.
     * @return la fórmula médica encontrada.
     * @throws BadRequestException si el identificador es inválido o la fórmula no existe.
     */
    FormulaMedica consultarFormulaMedica(Long formulaId) throws BadRequestException;

    /**
     * Registra una nueva fórmula médica, validando que la mascota exista y pertenezca
     * a un cliente registrado, que la cita exista, corresponda a la mascota, esté atendida, que el medicamento esté vigente y que la dosis,
     * frecuencia, duración y fecha de emisión sean válidas.
     *
     * @param formulaMedicaRq datos de la fórmula médica a registrar.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si los datos son inválidos o incumplen las reglas de negocio.
     */
    MiRespuestaRS guardarFormulaMedica(FormulaMedicaRq formulaMedicaRq) throws BadRequestException;

    /**
     * Actualiza una fórmula médica existente aplicando las mismas reglas de negocio
     * que el registro.
     *
     * @param formulaMedicaRq datos actualizados de la fórmula médica.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si los datos son inválidos, la fórmula no existe
     *                             o incumple las reglas de negocio.
     */
    MiRespuestaRS actualizarFormulaMedica(FormulaMedicaRq formulaMedicaRq) throws BadRequestException;
}
