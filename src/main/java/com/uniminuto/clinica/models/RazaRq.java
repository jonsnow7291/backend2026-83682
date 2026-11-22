package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Datos para registrar o actualizar una raza.
 */
@Data
public class RazaRq {

    private Integer razaId;
    private String nombre;
    private String especie;
}
