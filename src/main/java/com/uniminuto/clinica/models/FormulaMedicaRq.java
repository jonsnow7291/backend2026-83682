package com.uniminuto.clinica.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Datos para crear o editar una fórmula médica con uno o varios medicamentos.
 */
@Data
public class FormulaMedicaRq {

    /** Id de la fórmula (solo al editar). */
    private Long formulaId;
    /** Cita atendida de la que proviene la fórmula. */
    private Long citaId;
    /** Indicaciones generales. */
    private String indicaciones;
    /** Medicamentos prescritos (al menos uno). */
    private List<Detalle> detalles = new ArrayList<>();

    /** Línea de la fórmula. */
    @Data
    public static class Detalle {
        /** Medicamento (tipo MEDICAMENTO). */
        private Long medicamentoId;
        /** Dosis. */
        private String dosis;
        /** Frecuencia. */
        private String frecuencia;
        /** Duración en días. */
        private Integer duracionDias;
        /** Cantidad prescrita (unidades a despachar). */
        private Integer cantidadPrescrita;
        /** Vía de administración. */
        private String viaAdministracion;
        /** Indicaciones de la línea. */
        private String indicaciones;
    }
}
