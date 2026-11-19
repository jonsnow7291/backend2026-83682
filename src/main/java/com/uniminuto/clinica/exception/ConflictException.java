package com.uniminuto.clinica.exception;

/**
 * La operación entra en conflicto con el estado actual del recurso (duplicados, solapamientos,
 * doble procesamiento). Se traduce a HTTP 409.
 */
public class ConflictException extends RuntimeException {

    /**
     * @param message descripción del conflicto.
     */
    public ConflictException(String message) {
        super(message);
    }
}
