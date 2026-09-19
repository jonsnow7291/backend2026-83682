package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Clase que representa la solicitud de creación/actualización de una anotación de historia médica.
 */
@Data
public class AnotacionHistoriaRq {

    /**
     * Identificador de la anotación. Nulo al crear, obligatorio al actualizar.
     */
    private Long anotacionId;

    /**
     * Identificador de la historia médica a la que pertenece la anotación.
     */
    private Long historiaId;

    /**
     * Identificador del médico que realiza la anotación.
     */
    private Long medicoId;

    /**
     * Descripción/contenido clínico de la anotación.
     */
    private String descripcion;
}
