package com.uniminuto.clinica.models;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Segundo paso del inicio de sesión: código OTP recibido por correo.
 */
@Data
public class VerificarOtpRq {

    /** Identificador del desafío devuelto por el login. */
    @NotBlank(message = "El desafío es obligatorio")
    private String desafioId;

    /** Código de 6 dígitos. */
    @NotBlank(message = "El código es obligatorio")
    private String codigo;
}
