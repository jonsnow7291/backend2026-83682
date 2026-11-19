package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Producto de inventario: medicamento o utensilio. El stock se mantiene sincronizado con la
 * suma de sus lotes vigentes (ver {@code InventarioServiceImpl}).
 */
@Entity
@Table(name = "medicamento")
@Data
public class Medicamento {

    /** Identificador. */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código único de 8 caracteres alfanuméricos. */
    @Column(name = "codigo", length = 8, unique = true)
    private String codigo;

    /** Nombre comercial o genérico. */
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /** Descripción. */
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    /** Presentación (frasco, caja, etc.). */
    @Column(name = "presentacion", length = 100)
    private String presentacion;

    /** Tipo: MEDICAMENTO o UTENSILIO. */
    @Column(name = "tipo", nullable = false, length = 15)
    private String tipo = "MEDICAMENTO";

    /** Registro Invima (obligatorio para medicamentos). */
    @Column(name = "registro_invima", length = 50)
    private String registroInvima;

    /** Precio de compra de referencia. */
    @Column(name = "precio_compra", nullable = false)
    private BigDecimal precioCompra = BigDecimal.ZERO;

    /** Precio de venta. */
    @Column(name = "precio_venta", nullable = false)
    private BigDecimal precioVenta = BigDecimal.ZERO;

    /** Porcentaje de IVA del producto. */
    @Column(name = "iva_porcentaje", nullable = false)
    private BigDecimal ivaPorcentaje = BigDecimal.ZERO;

    /** Existencias vigentes (suma de lotes no vencidos). */
    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual = 0;

    /** Existencia mínima para alerta de stock bajo. */
    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 0;

    /** Fecha de ingreso del producto al catálogo. */
    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    /** Fecha prevista del próximo pedido. */
    @Column(name = "fecha_proximo_pedido")
    private LocalDate fechaProximoPedido;

    /** Indica si el producto está activo. */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    /** Fecha de creación del registro. */
    @Column(name = "fecha_creacion_registro", nullable = false)
    private LocalDateTime fechaCreacionRegistro;

    /** Fecha de última modificación. */
    @Column(name = "fecha_modificacion_registro")
    private LocalDateTime fechaModificacionRegistro;
}
