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

    /** {@code true} si falta el segundo paso: el código OTP enviado por correo. */
    private boolean otpRequerido;

    /** Identificador del desafío OTP (solo cuando {@code otpRequerido}). */
    private String desafioId;

    /** Correo enmascarado al que se envió el código (solo cuando {@code otpRequerido}). */
    private String correoEnmascarado;

    /** Respuesta con sesión completa (sin segundo factor pendiente). */
    public LoginRs(String token, String tipo, long expiraEnSegundos, SesionRs usuario) {
        this(token, tipo, expiraEnSegundos, usuario, false, null, null);
    }
}
