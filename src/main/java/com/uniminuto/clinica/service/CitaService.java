package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de negocio para la gestión de citas de la clínica veterinaria.
 */
public interface CitaService {

    /**
     * Lista las citas cuya fecha y hora se encuentren dentro del rango indicado,
     * ordenadas de la más reciente a la más antigua.
     *
     * @param fechaInicial límite inferior del rango (inclusive).
     * @param fechaFinal   límite superior del rango (inclusive).
     * @return lista de citas dentro del rango.
     * @throws BadRequestException si las fechas son nulas o el rango es inválido.
     */
    List<Cita> listarCitasPorFecha(LocalDateTime fechaInicial, LocalDateTime fechaFinal) throws BadRequestException;

    /**
     * Registra una nueva cita.
     *
     * @param citaRq datos de la cita a registrar.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si los datos son inválidos o las referencias no existen.
     */
    MiRespuestaRS guardarCita(CitaRq citaRq) throws BadRequestException;

    /**
     * Actualiza una cita existente.
     *
     * @param citaRq datos actualizados de la cita.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si los datos son inválidos o las referencias no existen.
     */
    MiRespuestaRS actualizarCita(CitaRq citaRq) throws BadRequestException;
}
