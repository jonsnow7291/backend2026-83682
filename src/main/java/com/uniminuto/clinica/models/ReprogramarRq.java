package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Datos para reprogramar una cita.
 */
@Data
public class ReprogramarRq {

    /** Cita a reprogramar. */
    private Long citaId;
    /** Nueva fecha y hora de inicio. */
    private LocalDateTime fechaHora;
    /** Nueva duración (opcional; conserva la anterior si no se envía). */
    private Integer duracionMinutos;
}
