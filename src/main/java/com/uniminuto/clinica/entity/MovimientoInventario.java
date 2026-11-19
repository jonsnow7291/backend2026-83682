package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entrada o salida de inventario, con el usuario responsable y, si aplica, el despacho y la fórmula.
 */
@Entity
@Table(name = "movimiento_inventario")
@Data
public class MovimientoInventario {

    /** Identificador. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Medicamento afectado. */
    @Column(name = "medicamento_id", nullable = false)
    private Long medicamentoId;

    /** Lote afectado. */
    @Column(name = "lote_id")
    private Long loteId;

    /** ENTRADA o SALIDA. */
    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo;

    /** Cantidad movida (siempre positiva). */
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    /** Fecha y hora. */
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    /** Usuario que realizó el movimiento. */
    @Column(name = "usuario_id")
    private Long usuarioId;

    /** Fórmula asociada (salidas por despacho). */
    @Column(name = "formula_medica_id")
    private Long formulaMedicaId;

    /** Despacho asociado (salidas por despacho). */
    @Column(name = "despacho_id")
    private Long despachoId;

    /** Motivo o descripción. */
    @Column(name = "motivo")
    private String motivo;
}
