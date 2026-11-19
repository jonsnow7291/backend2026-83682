package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pago (parcial o total) siempre ligado a una factura.
 */
@Entity
@Table(name = "pago")
@Data
public class Pago {

    /** Identificador. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Factura pagada. */
    @Column(name = "factura_id", nullable = false)
    private Long facturaId;

    /** Valor pagado. */
    @Column(name = "valor", nullable = false)
    private BigDecimal valor;

    /** Método: EFECTIVO, TARJETA, TRANSFERENCIA. */
    @Column(name = "metodo", nullable = false, length = 20)
    private String metodo;

    /** Referencia del pago. */
    @Column(name = "referencia", length = 100)
    private String referencia;

    /** Fecha del pago. */
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    /** Usuario que registró el pago. */
    @Column(name = "usuario_id")
    private Long usuarioId;
}
