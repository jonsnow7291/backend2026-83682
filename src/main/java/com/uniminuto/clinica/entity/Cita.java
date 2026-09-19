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
 * Entidad JPA que representa una cita médica agendada para una mascota.
 * Mapea la tabla {@code cita}.
 */
@Entity
@Table(name = "cita")
@Data
public class Cita {

    /**
     * Identificador único de la cita, autogenerado por la base de datos.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Cliente que solicita la cita.
     */
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /**
     * Mascota para la cual se agenda la cita.
     */
    @ManyToOne
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    /**
     * Médico veterinario que atenderá la cita.
     */
    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    /**
     * Fecha y hora programada para la cita.
     */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    /**
     * Estado actual de la cita (ej. programada, atendida, cancelada).
     */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    /**
     * Motivo de la cita.
     */
    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;
}
