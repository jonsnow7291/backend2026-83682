package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una fórmula médica (prescripción) generada en una cita,
 * asociada a un medicamento del inventario. Mapea la tabla {@code formula_medica}.
 */
@Entity
@Table(name = "formula_medica")
@Data
public class FormulaMedica {

    /**
     * Identificador único de la fórmula médica, autogenerado por la base de datos.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Cita en la que se generó la fórmula médica.
     */
    @ManyToOne
    @JoinColumn(name = "cita_id", nullable = false)
    private Cita cita;

    /**
     * Medicamento del inventario recetado en la fórmula.
     */
    @ManyToOne
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    /**
     * Dosis prescrita del medicamento.
     */
    @Column(name = "dosis", nullable = false, columnDefinition = "TEXT")
    private String dosis;

    /**
     * Frecuencia de administración del medicamento (ej. "Cada 8 horas").
     */
    @Column(name = "frecuencia", nullable = false, length = 100)
    private String frecuencia;

    /**
     * Duración del tratamiento expresada en días.
     */
    @Column(name = "duracion_dias", nullable = false)
    private Integer duracionDias;

    /**
     * Indicaciones adicionales para la administración del medicamento.
     */
    @Column(name = "indicaciones", columnDefinition = "TEXT")
    private String indicaciones;

    /**
     * Fecha y hora de emisión de la fórmula. Nunca es anterior a la fecha de la cita.
     */
    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    /**
     * Fecha de creación de la fórmula médica.
     */
    @Column(name = "fecha_creacion_registro", nullable = false)
    private LocalDateTime fechaCreacionRegistro;

    /**
     * Fecha de la última actualización de la fórmula médica.
     */
    @Column(name = "fecha_actualizacion_registro")
    private LocalDateTime fechaActualizacionRegistro;
}
