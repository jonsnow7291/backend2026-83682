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
 * Entidad JPA que representa la historia médica de una mascota (el paciente).
 * Mapea la tabla {@code historia_medica}. Está relacionada con {@link AnotacionHistoria},
 * donde se registran las anotaciones clínicas a lo largo del tiempo.
 */
@Entity
@Table(name = "historia_medica")
@Data
public class HistoriaMedica {

    /**
     * Identificador único de la historia médica, autogenerado por la base de datos.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mascota (paciente) a quien pertenece la historia médica.
     */
    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Mascota mascota;

    /**
     * Fecha de creación de la historia médica.
     */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}
