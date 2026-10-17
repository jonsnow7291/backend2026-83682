package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Clase que representa la solicitud de creación/actualización de una fórmula médica.
 */
@Data
public class FormulaMedicaRq {

    /**
     * Identificador de la fórmula médica. Nulo al crear, obligatorio al actualizar.
     */
    private Long formulaId;

    /**
     * Identificador de la mascota a la que se le prescribe la fórmula. Se valida
     * que corresponda a la mascota de la cita indicada.
     */
    private Integer mascotaId;

    /**
     * Identificador de la cita (consulta) en la que se emite la fórmula.
     */
    private Long citaId;

    /**
     * Identificador del medicamento del inventario que se prescribe.
     */
    private Long medicamentoId;

    /**
     * Dosis prescrita del medicamento (ej. "5 ml", "1 tableta").
     */
    private String dosis;

    /**
     * Frecuencia de administración del medicamento (ej. "Cada 8 horas").
     */
    private String frecuencia;

    /**
     * Duración del tratamiento expresada en días.
     */
    private Integer duracionDias;

    /**
     * Indicaciones adicionales para la administración del medicamento (opcional).
     */
    private String indicaciones;

    /**
     * Fecha y hora de emisión de la fórmula. Opcional: si no se envía, se toma
     * la fecha y hora actual. No puede ser anterior a la fecha de la consulta.
     */
    private LocalDateTime fechaEmision;
}
