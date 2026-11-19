package com.uniminuto.clinica.exception;

/**
 * Credenciales inválidas o ausencia de autenticación. Se traduce a HTTP 401.
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * @param message descripción del problema de autenticación.
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}
