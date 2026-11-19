package com.uniminuto.clinica.models;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Respuesta del login: token JWT y datos básicos del usuario autenticado.
 */
@Data
@AllArgsConstructor
public class LoginRs {

    /** Token JWT a enviar como {@code Authorization: Bearer <token>}. */
    private String token;

    /** Tipo de token (siempre {@code Bearer}). */
    private String tipo;

    /** Segundos de vigencia del token. */
    private long expiraEnSegundos;

    /** Datos del usuario autenticado. */
    private SesionRs usuario;
}
