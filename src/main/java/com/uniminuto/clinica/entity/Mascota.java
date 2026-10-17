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
 * Entidad JPA que representa una mascota (paciente) de la clínica. Mapea la tabla
 * {@code mascota}. Cada mascota pertenece a un cliente y tiene una raza.
 */
@Entity
@Table(name = "mascota")
@Data
public class Mascota {

    /**
     * Identificador único de la mascota, autogenerado por la base de datos.
     */
    @Id
    @Column(name = "mascota_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer mascotaId;

    /**
     * Nombre de la mascota.
     */
    @Column(name = "nombre_mascota", nullable = false)
    private String nombreMascota;

    /**
     * Edad de la mascota en años.
     */
    @Column(name = "edad", nullable = false)
    private Integer edad;

    /**
     * Fecha en que se registró la mascota.
     */
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    /**
     * Fecha de la última modificación de los datos de la mascota.
     */
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    /**
     * Raza de la mascota.
     */
    @ManyToOne
    @JoinColumn(name = "raza_id")
    private Raza raza;

    /**
     * Cliente propietario de la mascota.
     */
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
}
