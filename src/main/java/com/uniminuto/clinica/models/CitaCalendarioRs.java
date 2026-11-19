package com.uniminuto.clinica.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Cita lista para pintarse en un calendario (vistas día, semana y mes).
 */
@Data
@AllArgsConstructor
public class CitaCalendarioRs {

    /** Id de la cita. */
    private Long id;
    /** Título (mascota - propietario). */
    private String titulo;
    /** Inicio. */
    private LocalDateTime inicio;
    /** Fin. */
    private LocalDateTime fin;
    /** Estado. */
    private String estado;
    /** Nombre de la mascota. */
    private String mascota;
    /** Nombre del propietario. */
    private String propietario;
    /** Motivo de la cita. */
    private String motivo;
    /** Id del médico. */
    private Long medicoId;
    /** Nombre del médico. */
    private String medico;
}
