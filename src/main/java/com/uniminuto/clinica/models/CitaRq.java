package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Clase que representa la solicitud de creación/actualización de una cita.
 */
@Data
public class CitaRq {

    /**
     * Identificador de la cita. Nulo al crear, obligatorio al actualizar.
     */
    private Long citaId;

    /**
     * Identificador del cliente que solicita la cita.
     */
    private Long clienteId;

    /**
     * Identificador de la mascota para la cual se agenda la cita.
     */
    private Integer mascotaId;

    /**
     * Identificador del médico que atenderá la cita.
     */
    private Long medicoId;

    /**
     * Fecha y hora programada para la cita.
     */
    private LocalDateTime fechaHora;

    /**
     * Estado de la cita (ej. programada, atendida, cancelada).
     */
    private String estado;

    /**
     * Motivo de la cita.
     */
    private String motivo;
}
