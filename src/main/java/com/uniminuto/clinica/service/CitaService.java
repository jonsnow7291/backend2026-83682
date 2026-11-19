package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaCalendarioRs;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.ReprogramarRq;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Operaciones de negocio sobre citas médicas: agenda, reglas de disponibilidad y estados.
 */
public interface CitaService {

    /**
     * Lista citas por rango de fechas (un MEDICO solo ve las suyas).
     *
     * @param fechaInicial inicio del rango (opcional).
     * @param fechaFinal   fin del rango (opcional).
     * @return citas, la más reciente primero.
     * @throws BadRequestException si el rango es inválido.
     */
    List<Cita> listarCitasPorFecha(LocalDateTime fechaInicial, LocalDateTime fechaFinal) throws BadRequestException;

    /**
     * Consulta una cita.
     *
     * @param citaId identificador.
     * @return la cita.
     * @throws BadRequestException si el id es inválido.
     */
    Cita consultarCita(Long citaId) throws BadRequestException;

    /**
     * Lista las citas atendidas (ATENDIDA o FINALIZADA) de una mascota.
     *
     * @param mascotaId mascota.
     * @return citas atendidas.
     * @throws BadRequestException si la mascota no existe.
     */
    List<Cita> listarCitasAtendidasPorMascota(Integer mascotaId) throws BadRequestException;

    /**
     * Registra una cita validando las reglas de agenda.
     *
     * @param citaRq datos de la cita.
     * @return respuesta de éxito.
     * @throws BadRequestException si se incumple alguna regla.
     */
    MiRespuestaRS guardarCita(CitaRq citaRq) throws BadRequestException;

    /**
     * Edita una cita pendiente o confirmada.
     *
     * @param citaRq datos con id.
     * @return respuesta de éxito.
     * @throws BadRequestException si se incumple alguna regla.
     */
    MiRespuestaRS actualizarCita(CitaRq citaRq) throws BadRequestException;

    /**
     * Cancela una cita conservando su historial.
     *
     * @param citaId identificador.
     * @param motivo motivo de cancelación (opcional).
     * @return respuesta de éxito.
     * @throws BadRequestException si no se puede cancelar.
     */
    MiRespuestaRS cancelarCita(Long citaId, String motivo) throws BadRequestException;

    /**
     * Cambia fecha/hora de una cita aplicando las mismas reglas que al crearla.
     *
     * @param rq nueva fecha y duración.
     * @return respuesta de éxito.
     */
    MiRespuestaRS reprogramar(ReprogramarRq rq);

    /**
     * Confirma una cita pendiente.
     *
     * @param citaId cita.
     * @return respuesta de éxito.
     */
    MiRespuestaRS confirmar(Long citaId);

    /**
     * Marca una cita como atendida.
     *
     * @param citaId cita.
     * @return respuesta de éxito.
     */
    MiRespuestaRS marcarAtendida(Long citaId);

    /**
     * Citas listas para el calendario del médico.
     *
     * @param medicoId médico (un MEDICO solo puede consultar el suyo; si no se envía usa el propio).
     * @param desde    inicio del rango.
     * @param hasta    fin del rango.
     * @return citas del calendario.
     */
    List<CitaCalendarioRs> calendario(Long medicoId, LocalDateTime desde, LocalDateTime hasta);

    /**
     * Historial completo de citas de una mascota.
     *
     * @param mascotaId mascota.
     * @return citas, la más reciente primero.
     */
    List<Cita> historialMascota(Integer mascotaId);

    /**
     * Resumen de cantidad de citas por estado.
     *
     * @param medicoId médico opcional.
     * @param desde    inicio.
     * @param hasta    fin.
     * @return mapa estado → cantidad (incluye TOTAL).
     */
    Map<String, Long> resumenPorEstado(Long medicoId, LocalDateTime desde, LocalDateTime hasta);

    /**
     * Reporte Excel de las citas del médico.
     *
     * @param medicoId médico opcional.
     * @param desde    inicio.
     * @param hasta    fin.
     * @return bytes del .xlsx.
     */
    byte[] reporteExcel(Long medicoId, LocalDateTime desde, LocalDateTime hasta);
}
