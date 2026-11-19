package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;

/**
 * Franja de disponibilidad semanal de un veterinario.
 */
@Entity
@Table(name = "horario_veterinario")
@Data
public class HorarioVeterinario {

    /** Identificador. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Médico dueño del horario. */
    @Column(name = "medico_id", nullable = false)
    private Long medicoId;

    /** Día de la semana ISO: 1 = lunes ... 7 = domingo. */
    @Column(name = "dia_semana", nullable = false)
    private Integer diaSemana;

    /** Hora de inicio de la franja. */
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    /** Hora de fin de la franja. */
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    /** Indica si la franja está activa. */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
