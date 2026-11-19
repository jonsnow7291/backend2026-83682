package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Datos de una atención médica (apertura y registro clínico).
 */
@Data
public class AtencionRq {

    /** Id de la atención (al registrar/actualizar). */
    private Long id;
    /** Cita de origen (al abrir). */
    private Long citaId;
    /** Tarifa de la consulta (opcional: por defecto la consulta de la especialidad). */
    private Long tarifaId;
    /** Motivo de consulta. */
    private String motivo;
    /** Síntomas. */
    private String sintomas;
    /** Diagnóstico. */
    private String diagnostico;
    /** Observaciones. */
    private String observaciones;
    /** Recomendaciones. */
    private String recomendaciones;
}
