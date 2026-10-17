package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de negocio para la gestión de citas de la clínica veterinaria.
 * Define las operaciones CRUD y validaciones de reglas de negocio para citas.
 */
public interface CitaService {

    /**
     * Lista las citas cuya fecha y hora se encuentren dentro del rango indicado,
     * ordenadas de la más reciente a la más antigua. Si los parámetros son nulos,
     * retorna todas las citas del sistema ordenadas por fecha.
     *
     * @param fechaInicial límite inferior del rango (inclusive, opcional).
     * @param fechaFinal   límite superior del rango (inclusive, opcional).
     * @return lista de citas dentro del rango o todas las existentes.
     * @throws BadRequestException si solo se envía una fecha o la fecha inicial es posterior a la final.
     */
    List<Cita> listarCitasPorFecha(LocalDateTime fechaInicial, LocalDateTime fechaFinal) throws BadRequestException;

    /**
     * Consulta una cita por su identificador único.
     *
     * @param citaId identificador de la cita.
     * @return la entidad Cita correspondiente.
     * @throws BadRequestException si el identificador es inválido o no existe.
     */
    Cita consultarCita(Long citaId) throws BadRequestException;

    /**
     * Registra una nueva cita aplicando las reglas de negocio (15 min veterinario, mascota, fecha actual, estados).
     *
     * @param citaRq datos de la cita a registrar.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si los datos son inválidos o hay conflictos de horario.
     */
    MiRespuestaRS guardarCita(CitaRq citaRq) throws BadRequestException;

    /**
     * Lista las citas en estado atendida de una mascota, de la más reciente a la más antigua.
     *
     * @param mascotaId identificador de la mascota.
     * @return citas atendidas de la mascota.
     * @throws BadRequestException si el identificador es inválido o la mascota no existe.
     */
    List<Cita> listarCitasAtendidasPorMascota(Integer mascotaId) throws BadRequestException;

    /**
     * Actualiza una cita existente verificando las restricciones según su estado actual.
     *
     * @param citaRq datos actualizados de la cita.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si la cita no existe, está cancelada o es atendida con modificaciones históricas.
     */
    MiRespuestaRS actualizarCita(CitaRq citaRq) throws BadRequestException;

    /**
     * Cancela una cita de forma explícita cambiando su estado a 'Cancelada'.
     *
     * @param citaId identificador de la cita a cancelar.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si la cita no existe o ya está en estado final.
     */
    MiRespuestaRS cancelarCita(Long citaId) throws BadRequestException;
}
