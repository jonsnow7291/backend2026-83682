package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Datos para crear o actualizar un usuario del sistema.
 */
@Data
public class UsuarioRq {

    private Long id;
    private String username;
    /** Obligatoria al crear; opcional al actualizar (si viene, reemplaza la actual). */
    private String password;
    private String email;
    private String rol;
    /** Datos profesionales; obligatorios cuando el rol es MEDICO (se crean/actualizan junto con el usuario). */
    private MedicoRq medico;
}
