package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Despacho (dispensación) de una fórmula médica. Se crea automáticamente al emitir la fórmula.
 */
@Entity
@Table(name = "despacho")
@Data
public class Despacho {

    /** Identificador. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Fórmula despachada (1 a 1). */
    @Column(name = "formula_id", nullable = false, unique = true)
    private Long formulaId;

    /** PENDIENTE_DESPACHO, EN_PREPARACION, DESPACHO_PARCIAL, DESPACHADO, PENDIENTE_DISPONIBILIDAD o CANCELADO. */
    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    /** Último usuario de despacho que intervino. */
    @Column(name = "usuario_id")
    private Long usuarioId;

    /** Fecha de creación. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    /** Fecha en que pasó a preparación. */
    @Column(name = "fecha_preparacion")
    private LocalDateTime fechaPreparacion;

    /** Fecha de la última confirmación. */
    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    /** Observaciones. */
    @Column(name = "observaciones", length = 500)
    private String observaciones;

    /** Líneas solicitadas y entregadas. */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "despacho_id", nullable = false)
    private List<DespachoDetalle> detalles = new ArrayList<>();
}
