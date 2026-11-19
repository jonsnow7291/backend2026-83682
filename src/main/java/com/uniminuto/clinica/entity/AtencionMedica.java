package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Atención médica abierta a partir de una cita: síntomas, diagnóstico y recomendaciones.
 */
@Entity
@Table(name = "atencion_medica")
@Data
public class AtencionMedica {

    /** Identificador. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Cita de origen (una atención por cita). */
    @Column(name = "cita_id", nullable = false, unique = true)
    private Long citaId;

    /** Médico que atiende. */
    @Column(name = "medico_id", nullable = false)
    private Long medicoId;

    /** Tarifa de la consulta que se cobrará. */
    @Column(name = "tarifa_id")
    private Long tarifaId;

    /** Motivo de consulta. */
    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    /** Síntomas. */
    @Column(name = "sintomas", columnDefinition = "TEXT")
    private String sintomas;

    /** Diagnóstico. */
    @Column(name = "diagnostico", columnDefinition = "TEXT")
    private String diagnostico;

    /** Observaciones. */
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    /** Recomendaciones al propietario. */
    @Column(name = "recomendaciones", columnDefinition = "TEXT")
    private String recomendaciones;

    /** EN_CURSO o FINALIZADA. */
    @Column(name = "estado", nullable = false, length = 15)
    private String estado;

    /** Inicio de la atención. */
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    /** Fin de la atención. */
    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;
}
