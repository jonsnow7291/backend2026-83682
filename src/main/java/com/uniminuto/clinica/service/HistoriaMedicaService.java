package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de negocio para el CRUD de historias médicas de las mascotas.
 */
public interface HistoriaMedicaService {

    /**
     * Lista las historias médicas, ordenadas de la más reciente a la más antigua.
     * Si se proporcionan ambas fechas, filtra las historias creadas dentro del rango.
     * Si no se proporciona ninguna, lista todas las historias.
     *
     * @param fechaInicial límite inferior del rango (inclusive), o {@code null} para no filtrar.
     * @param fechaFinal   límite superior del rango (inclusive), o {@code null} para no filtrar.
     * @return lista de historias médicas.
     * @throws BadRequestException si se proporciona solo una de las dos fechas o el rango es inválido.
     */
    List<HistoriaMedica> listarHistoriasMedicas(LocalDateTime fechaInicial, LocalDateTime fechaFinal)
            throws BadRequestException;

    /**
     * Registra una nueva historia médica para una mascota.
     *
     * @param historiaMedicaRq datos de la historia médica a registrar.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si los datos son inválidos o la mascota no existe.
     */
    MiRespuestaRS guardarHistoriaMedica(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException;

    /**
     * Actualiza una historia médica existente.
     *
     * @param historiaMedicaRq datos actualizados de la historia médica.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si los datos son inválidos, la historia o la mascota no existen.
     */
    MiRespuestaRS actualizarHistoriaMedica(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException;

    /**
     * Elimina una historia médica existente.
     *
     * @param historiaId identificador de la historia médica a eliminar.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si el identificador es inválido o la historia no existe.
     */
    MiRespuestaRS eliminarHistoriaMedica(Long historiaId) throws BadRequestException;
}
