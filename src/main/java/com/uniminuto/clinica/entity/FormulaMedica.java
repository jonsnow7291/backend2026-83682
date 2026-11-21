package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Fórmula médica emitida por el veterinario, con uno o varios medicamentos. Crearla NO descuenta
 * inventario: eso ocurre solo al confirmar el despacho.
 */
@Entity
@Table(name = "formula_medica")
@Data
public class FormulaMedica {

    /** Identificador. */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Cita de la que proviene la fórmula. */
    @ManyToOne
    @JoinColumn(name = "cita_id", nullable = false)
    private Cita cita;

    /** Médico que la emite. */
    @Column(name = "medico_id")
    private Long medicoId;

    /** Atención médica asociada, si existe. */
    @Column(name = "atencion_id")
    private Long atencionId;

    /** Indicaciones generales. */
    @Column(name = "indicaciones", columnDefinition = "TEXT")
    private String indicaciones;

    /**
     * Estado de la fórmula: BORRADOR (el médico aún la edita; no genera despacho) o CONFIRMADA
     * (el médico la confirmó y quedó disponible para el despacho). Regla 5.8-2 del enunciado.
     */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "BORRADOR";

    /** Fecha y hora en que el médico confirmó la fórmula. */
    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    /** Estado del despacho de la fórmula (SIN_CONFIRMAR mientras sea borrador; luego refleja el despacho). */
    @Column(name = "estado_despacho", nullable = false, length = 30)
    private String estadoDespacho = "SIN_CONFIRMAR";

    /** Usuario que la creó. */
    @Column(name = "usuario_id")
    private Long usuarioId;

    /** Fecha de emisión. */
    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    /** Fecha de creación del registro. */
    @Column(name = "fecha_creacion_registro", nullable = false)
    private LocalDateTime fechaCreacionRegistro;

    /** Fecha de última actualización. */
    @Column(name = "fecha_actualizacion_registro")
    private LocalDateTime fechaActualizacionRegistro;

    /** Medicamentos prescritos. */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "formula_id", nullable = false)
    private List<FormulaMedicaDetalle> detalles = new ArrayList<>();
}
