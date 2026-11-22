package com.uniminuto.clinica.exception;

/**
 * Se superó el límite de intentos permitido: HTTP 429.
 */
public class TooManyRequestsException extends RuntimeException {

    /**
     * @param message detalle para el usuario.
     */
    public TooManyRequestsException(String message) {
        super(message);
    }
}
