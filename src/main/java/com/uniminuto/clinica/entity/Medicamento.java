package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un medicamento del inventario de la clínica.
 * Mapea la tabla {@code medicamento}.
 */
@Entity
@Table(name = "medicamento")
@Data
public class Medicamento {

    /**
     * Identificador único del medicamento, autogenerado por la base de datos.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre comercial del medicamento.
     */
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /**
     * Descripción del medicamento.
     */
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Presentación del medicamento (ej. Tabletas 500mg).
     */
    @Column(name = "presentacion", length = 100)
    private String presentacion;

    /**
     * Fecha de compra del lote de medicamento.
     */
    @Column(name = "fecha_compra", nullable = false)
    private LocalDate fechaCompra;

    /**
     * Fecha de vencimiento del lote de medicamento.
     */
    @Column(name = "fecha_vence", nullable = false)
    private LocalDate fechaVence;

    /**
     * Fecha de creación del registro del medicamento.
     */
    @Column(name = "fecha_creacion_registro", nullable = false)
    private LocalDateTime fechaCreacionRegistro;

    /**
     * Fecha de la última modificación del registro del medicamento.
     */
    @Column(name = "fecha_modificacion_registro")
    private LocalDateTime fechaModificacionRegistro;
}
