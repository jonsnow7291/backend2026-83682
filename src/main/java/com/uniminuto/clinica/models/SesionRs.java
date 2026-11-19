package com.uniminuto.clinica.models;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Datos del usuario autenticado (para el frontend y {@code GET /auth/me}).
 */
@Data
@AllArgsConstructor
public class SesionRs {

    /** Identificador del usuario. */
    private Long id;

    /** Nombre de usuario. */
    private String username;

    /** Rol: ADMIN, SECRETARIA, MEDICO o DESPACHO_MEDICO. */
    private String rol;

    /** Id del médico vinculado (solo rol MEDICO), o {@code null}. */
    private Long medicoId;
}
