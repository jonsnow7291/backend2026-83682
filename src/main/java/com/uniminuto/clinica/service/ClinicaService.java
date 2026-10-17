package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.exception.BadRequestException;

/**
 * Servicio de diagnóstico para verificar que la capa de servicios responde.
 */
public interface ClinicaService {

    /**
     * Devuelve un texto fijo que confirma que el servicio funciona.
     *
     * @return mensaje de confirmación.
     * @throws BadRequestException si ocurre un error al procesar la solicitud.
     */
    String testService2() throws BadRequestException;

    /**
     * Devuelve una respuesta estructurada que confirma que el servicio funciona.
     *
     * @return respuesta con estado y mensaje.
     * @throws BadRequestException si ocurre un error al procesar la solicitud.
     */
    MiRespuestaRS testService3() throws BadRequestException;
}
