package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalTime;

/**
 * Datos de una franja de horario de un veterinario.
 */
@Data
public class HorarioRq {

    /** Id (solo al actualizar). */
    private Long id;
    /** Médico. */
    private Long medicoId;
    /** Día ISO: 1 = lunes ... 7 = domingo. */
    private Integer diaSemana;
    /** Hora de inicio (HH:mm). */
    private LocalTime horaInicio;
    /** Hora de fin (HH:mm). */
    private LocalTime horaFin;
    /** Activo (true si no se envía). */
    private Boolean activo;
}
