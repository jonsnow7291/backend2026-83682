package com.uniminuto.clinica.exception;

/**
 * El recurso solicitado no existe. Se traduce a HTTP 404.
 */
public class NotFoundException extends RuntimeException {

    /**
     * @param message descripción del recurso que no se encontró.
     */
    public NotFoundException(String message) {
        super(message);
    }
}
