package com.uniminuto.clinica.exception;

import org.springframework.http.HttpStatus;

/**
 * Clase de excepción personalizada para manejar errores de solicitud incorrecta (Bad Request).
 * La lanza la capa de servicios cuando se incumple una validación o regla de negocio, y
 * {@link GlobalExceptionHandler} la traduce a una respuesta HTTP 400.
 */
public class BadRequestException extends RuntimeException {

    /**
     * Código HTTP asociado al error (siempre {@link HttpStatus#BAD_REQUEST}).
     */
    private final HttpStatus status;

    /**
     * Crea la excepción con un mensaje comprensible para el usuario.
     *
     * @param message descripción del error de validación o de negocio.
     */
    public BadRequestException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    /**
     * Obtiene el código HTTP asociado al error.
     *
     * @return código HTTP 400.
     */
    public HttpStatus getStatus() {
        return status;
    }
}
