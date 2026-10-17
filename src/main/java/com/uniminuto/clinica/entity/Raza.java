package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una raza de mascota. Mapea la tabla {@code raza}.
 */
@Entity
@Table(name = "raza")
@Data
public class Raza {

    /**
     * Identificador único de la raza, autogenerado por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "raza_id")
    private Integer razaId;

    /**
     * Nombre de la raza (ej. Golden Retriever).
     */
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /**
     * Especie a la que pertenece la raza (ej. Canino, Felino).
     */
    @Column(name = "especie", nullable = false, length = 100)
    private String especie;

    /**
     * Fecha de creación del registro.
     */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de la última modificación del registro.
     */
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;
}
