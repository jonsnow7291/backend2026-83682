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
     * Identificador del cliente al que pertenece la mascota. Opcional: si se envía,
     * se valida que la mascota seleccionada le pertenezca.
     */
    private Long clienteId;

    /**
     * Identificador de la mascota (paciente) dueña de la historia médica.
     */
    private Integer mascotaId;

    /**
     * Identificador de la cita asociada a la historia. Opcional: si se envía, se
     * valida que la cita exista, corresponda a la mascota y esté en estado
     * {@code atendida}.
     */
    private Long citaId;
}