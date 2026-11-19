package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lote de un medicamento con su fecha de vencimiento y cantidad disponible.
 */
@Entity
@Table(name = "lote_medicamento")
@Data
public class LoteMedicamento {

    /** Identificador. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Medicamento al que pertenece. */
    @Column(name = "medicamento_id", nullable = false)
    private Long medicamentoId;

    /** Número de lote del proveedor. */
    @Column(name = "numero_lote", nullable = false, length = 50)
    private String numeroLote;

    /** Cantidad con la que ingresó el lote. */
    @Column(name = "cantidad_inicial", nullable = false)
    private Integer cantidadInicial;

    /** Cantidad disponible actual (nunca negativa). */
    @Column(name = "cantidad_disponible", nullable = false)
    private Integer cantidadDisponible;

    /** Precio de compra unitario del lote. */
    @Column(name = "precio_compra", nullable = false)
    private BigDecimal precioCompra = BigDecimal.ZERO;

    /** Fecha de compra. */
    @Column(name = "fecha_compra", nullable = false)
    private LocalDate fechaCompra;

    /** Fecha de vencimiento (posterior a la compra). */
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    /** Fecha de registro. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
}
