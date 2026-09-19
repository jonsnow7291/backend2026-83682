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
     * Identificador de la mascota (paciente) dueña de la historia médica.
     */
    private Integer mascotaId;
}
