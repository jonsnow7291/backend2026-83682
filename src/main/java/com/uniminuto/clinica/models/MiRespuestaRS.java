package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Respuesta homogénea de las operaciones de escritura (guardar, actualizar, eliminar).
 */
@Data
public class MiRespuestaRS {

    /**
     * Código de estado de la operación (200 cuando es exitosa).
     */
    private int status;

    /**
     * Mensaje descriptivo del resultado de la operación.
     */
    private String message;

    /** Id del recurso creado (solo en altas que lo informan, p. ej. citas). */
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    private Long id;
}
