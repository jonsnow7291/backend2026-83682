package com.uniminuto.clinica.models;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos para crear o actualizar un producto de inventario (medicamento o utensilio).
 */
@Data
public class MedicamentoRq {

    /** Id del producto (solo en actualización). */
    private Long id;
    /** Código de 8 caracteres alfanuméricos. */
    private String codigo;
    /** Nombre. */
    private String nombre;
    /** Descripción. */
    private String descripcion;
    /** Presentación. */
    private String presentacion;
    /** MEDICAMENTO o UTENSILIO. */
    private String tipo;
    /** Registro Invima (obligatorio si es medicamento). */
    private String registroInvima;
    /** Precio de compra de referencia. */
    private BigDecimal precioCompra;
    /** Precio de venta. */
    private BigDecimal precioVenta;
    /** IVA en porcentaje (0 si no se envía). */
    private BigDecimal ivaPorcentaje;
    /** Stock mínimo para alertas. */
    private Integer stockMinimo;
    /** Fecha prevista del próximo pedido. */
    private LocalDate fechaProximoPedido;
    /** Activo (true si no se envía). */
    private Boolean activo;
}
