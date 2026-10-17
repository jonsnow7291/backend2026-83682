package com.uniminuto.clinica.exception.dto;

import lombok.Data;

/**
 * Clase que representa la estructura de la respuesta de error
 * para las excepciones manejadas por el GlobalExceptionHandler.
 */
@Data
public class ErrorResponse {

    /**
     * Código HTTP del error (ej. 400, 405, 500).
     */
    private int status;

    /**
     * Nombre corto del tipo de error (ej. {@code BAD REQUEST}).
     */
    private String error;

    /**
     * Mensaje comprensible para el usuario.
     */
    private String message;

    /**
     * Crea una respuesta de error.
     *
     * @param status  código HTTP del error.
     * @param error   nombre corto del tipo de error.
     * @param message mensaje comprensible para el usuario.
     */
    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }

}
