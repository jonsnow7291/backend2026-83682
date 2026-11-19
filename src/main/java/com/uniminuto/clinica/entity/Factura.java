package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Factura de una cita atendida: servicio + medicamentos efectivamente despachados. Nunca se borra;
 * se anula conservando el histórico.
 */
@Entity
@Table(name = "factura")
@Data
public class Factura {

    /** Identificador. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Número de factura (FAC-aaaa-000001). */
    @Column(name = "numero", unique = true, length = 20)
    private String numero;

    /** Cita facturada. */
    @Column(name = "cita_id", nullable = false)
    private Long citaId;

    /** Atención médica asociada. */
    @Column(name = "atencion_id")
    private Long atencionId;

    /** Propietario. */
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    /** Mascota. */
    @Column(name = "mascota_id", nullable = false)
    private Integer mascotaId;

    /** Veterinario. */
    @Column(name = "medico_id", nullable = false)
    private Long medicoId;

    /** Especialidad. */
    @Column(name = "especializacion_id")
    private Long especializacionId;

    /** Suma de líneas antes de impuestos. */
    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal;

    /** Total de IVA. */
    @Column(name = "impuestos", nullable = false)
    private BigDecimal impuestos;

    /** Descuento aplicado. */
    @Column(name = "descuento", nullable = false)
    private BigDecimal descuento = BigDecimal.ZERO;

    /** Total a pagar. */
    @Column(name = "total", nullable = false)
    private BigDecimal total;

    /** Fecha de emisión. */
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    /** PENDIENTE, PARCIAL, PAGADA o ANULADA. */
    @Column(name = "estado_pago", nullable = false, length = 15)
    private String estadoPago;

    /** Motivo de anulación. */
    @Column(name = "motivo_anulacion")
    private String motivoAnulacion;

    /** Fecha de anulación. */
    @Column(name = "fecha_anulacion")
    private LocalDateTime fechaAnulacion;

    /** Usuario que la generó. */
    @Column(name = "usuario_id")
    private Long usuarioId;

    /** Líneas de la factura. */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "factura_id", nullable = false)
    private List<FacturaDetalle> detalles = new ArrayList<>();
}
