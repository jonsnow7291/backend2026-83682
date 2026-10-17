package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Clase que representa la solicitud de creación/actualización de una historia médica.
 */
@Data
public class HistoriaMedicaRq {

    /**
     * Identificador de la historia médica. Nulo al crear, obligatorio al actualizar.
     */
    private Long historiaId;

    /**
     * Identificador del cliente al que pertenece la mascota. Se valida que la
     * mascota seleccionada le pertenezca.
     */
    private Long clienteId;

    /**
     * Identificador de la mascota (paciente) dueña de la historia médica.
     */
    private Integer mascotaId;

    /**
     * Identificador de la cita asociada a la historia. Se valida que la cita exista,
     * corresponda a la mascota, esté en estado {@code atendida} y no tenga ya otra
     * historia clínica registrada.
     */
    private Long citaId;

    /**
     * Diagnóstico de la consulta (obligatorio).
     */
    private String diagnostico;

    /**
     * Observaciones generales de la consulta (opcional).
     */
    private String observaciones;
}