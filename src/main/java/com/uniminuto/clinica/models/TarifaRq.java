package com.uniminuto.clinica.models;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Datos para crear o modificar una tarifa.
 */
@Data
public class TarifaRq {

    /** Id (solo al actualizar). */
    private Long id;
    /** Especialidad. */
    private Long especializacionId;
    /** Servicio. */
    private String servicio;
    /** Valor antes de IVA. */
    private BigDecimal valor;
    /** IVA en porcentaje. */
    private BigDecimal ivaPorcentaje;
    /** Activa (true si no se envía). */
    private Boolean activa;
}
