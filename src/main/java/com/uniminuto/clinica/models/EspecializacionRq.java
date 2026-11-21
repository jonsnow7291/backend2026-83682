package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Datos para registrar o editar una especialidad veterinaria.
 */
@Data
public class EspecializacionRq {

    /** Id de la especialidad (solo al editar). */
    private Long id;
    /** Nombre (único). */
    private String nombre;
    /** Descripción libre. */
    private String descripcion;
    /** Código corto (único, hasta 10 caracteres alfanuméricos, guion o guion bajo). */
    private String codigoEspecializacion;
}
