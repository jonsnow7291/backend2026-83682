package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Línea de factura con el precio unitario y el IVA congelados al momento de facturar.
 */
@Entity
@Table(name = "factura_detalle")
@Data
public class FacturaDetalle {

    /** Identificador. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** SERVICIO o MEDICAMENTO. */
    @Column(name = "tipo", nullable = false, length = 15)
    private String tipo;

    /** Descripción de la línea. */
    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    /** Tarifa de origen (servicios). */
    @Column(name = "tarifa_id")
    private Long tarifaId;

    /** Medicamento de origen. */
    @Column(name = "medicamento_id")
    private Long medicamentoId;

    /** Cantidad. */
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    /** Precio unitario congelado. */
    @Column(name = "precio_unitario", nullable = false)
    private BigDecimal precioUnitario;

    /** IVA aplicado (%). */
    @Column(name = "iva_porcentaje", nullable = false)
    private BigDecimal ivaPorcentaje;

    /** Cantidad × precio. */
    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal;

    /** Valor del IVA de la línea. */
    @Column(name = "iva_valor", nullable = false)
    private BigDecimal ivaValor;
}
