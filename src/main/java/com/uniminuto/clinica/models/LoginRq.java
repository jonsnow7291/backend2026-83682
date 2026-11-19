package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Credenciales para iniciar sesión.
 */
@Data
public class LoginRq {

    /** Nombre de usuario. */
    @NotBlank(message = "El usuario es obligatorio")
    @Schema(example = "admin")
    private String username;

    /** Contraseña en texto plano (viaja solo en el login). */
    @NotBlank(message = "La contraseña es obligatoria")
    @Schema(example = "Clinica2026*")
    private String password;
}
