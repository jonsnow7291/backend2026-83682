package com.uniminuto.clinica.models;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos de una entrada de inventario (nuevo lote).
 */
@Data
public class LoteRq {

    /** Medicamento que ingresa. */
    private Long medicamentoId;
    /** Número de lote. */
    private String numeroLote;
    /** Cantidad que ingresa (mayor que cero). */
    private Integer cantidad;
    /** Precio de compra unitario. */
    private BigDecimal precioCompra;
    /** Fecha de compra. */
    private LocalDate fechaCompra;
    /** Fecha de vencimiento (posterior a la compra). */
    private LocalDate fechaVencimiento;
}
