package com.uniminuto.clinica.exception;

/**
 * Un servicio externo necesario (por ejemplo el correo SMTP) no está disponible: HTTP 503.
 */
public class ServiceUnavailableException extends RuntimeException {

    /**
     * @param message detalle para el usuario.
     */
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
