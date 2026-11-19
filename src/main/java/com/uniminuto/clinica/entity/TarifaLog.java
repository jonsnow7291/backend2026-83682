package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Historial de cambios de una tarifa (valor anterior y nuevo, usuario y fecha).
 */
@Entity
@Table(name = "tarifa_log")
@Data
public class TarifaLog {

    /** Identificador. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tarifa modificada. */
    @Column(name = "tarifa_id", nullable = false)
    private Long tarifaId;

    /** CREACION, MODIFICACION, ACTIVACION o INACTIVACION. */
    @Column(name = "accion", nullable = false, length = 20)
    private String accion;

    /** Valor anterior. */
    @Column(name = "valor_anterior")
    private BigDecimal valorAnterior;

    /** Valor nuevo. */
    @Column(name = "valor_nuevo")
    private BigDecimal valorNuevo;

    /** IVA anterior. */
    @Column(name = "iva_anterior")
    private BigDecimal ivaAnterior;

    /** IVA nuevo. */
    @Column(name = "iva_nuevo")
    private BigDecimal ivaNuevo;

    /** Usuario que hizo el cambio. */
    @Column(name = "usuario_id")
    private Long usuarioId;

    /** Fecha del cambio. */
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;
}
