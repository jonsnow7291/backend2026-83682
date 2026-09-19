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
 * Entidad JPA que representa una anotación clínica dentro de la historia médica de una mascota.
 * Mapea la tabla {@code anotacion_historia}.
 */
@Entity
@Table(name = "anotacion_historia")
@Data
public class AnotacionHistoria {

    /**
     * Identificador único de la anotación, autogenerado por la base de datos.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Historia médica a la que pertenece esta anotación.
     */
    @ManyToOne
    @JoinColumn(name = "historia_id", nullable = false)
    private HistoriaMedica historiaMedica;

    /**
     * Médico que realiza la anotación.
     */
    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    /**
     * Fecha en la que se realizó la anotación.
     */
    @Column(name = "fecha")
    private LocalDateTime fecha;

    /**
     * Descripción/contenido clínico de la anotación.
     */
    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;
}
