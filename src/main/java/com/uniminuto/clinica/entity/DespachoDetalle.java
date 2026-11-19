package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Línea de despacho: cantidad solicitada (copiada de la fórmula, inmodificable) y entregada.
 */
@Entity
@Table(name = "despacho_detalle")
@Data
public class DespachoDetalle {

    /** Identificador. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Línea de fórmula de origen. */
    @Column(name = "formula_detalle_id", nullable = false)
    private Long formulaDetalleId;

    /** Medicamento. */
    @Column(name = "medicamento_id", nullable = false)
    private Long medicamentoId;

    /** Cantidad solicitada por el médico. */
    @Column(name = "cantidad_solicitada", nullable = false)
    private Integer cantidadSolicitada;

    /** Cantidad entregada hasta el momento. */
    @Column(name = "cantidad_entregada", nullable = false)
    private Integer cantidadEntregada = 0;
}
