package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

/**
 * Servicio de negocio para la gestión de anotaciones dentro de una historia médica.
 */
public interface AnotacionHistoriaService {

    /**
     * Lista las anotaciones de una historia médica específica, ordenadas
     * de la más reciente a la más antigua.
     *
     * @param historiaId identificador de la historia médica.
     * @return lista de anotaciones de la historia médica indicada.
     * @throws BadRequestException si el identificador es inválido o la historia no existe.
     */
    List<AnotacionHistoria> listarAnotaciones(Long historiaId) throws BadRequestException;

    /**
     * Registra una nueva anotación en una historia médica.
     *
     * @param anotacionHistoriaRq datos de la anotación a registrar.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si los datos son inválidos o las referencias no existen.
     */
    MiRespuestaRS guardarAnotacionHistoria(AnotacionHistoriaRq anotacionHistoriaRq) throws BadRequestException;

    /**
     * Actualiza una anotación de historia médica existente.
     *
     * @param anotacionHistoriaRq datos actualizados de la anotación.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si los datos son inválidos o las referencias no existen.
     */
    MiRespuestaRS actualizarAnotacionHistoria(AnotacionHistoriaRq anotacionHistoriaRq) throws BadRequestException;
}
