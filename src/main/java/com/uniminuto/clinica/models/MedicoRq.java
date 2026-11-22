package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Datos para registrar o actualizar un médico veterinario.
 */
@Data
public class MedicoRq {

    private Long id;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String registroProfesional;
    private Integer especializacionId;
    /** Usuario de acceso asociado (opcional; debe tener rol MEDICO). */
    private Long usuarioId;
}
