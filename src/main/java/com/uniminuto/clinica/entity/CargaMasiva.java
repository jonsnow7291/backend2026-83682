package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Registro (log) de una carga masiva de inventario confirmada.
 */
@Entity
@Table(name = "carga_masiva")
@Data
public class CargaMasiva {

    /** Identificador. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre del archivo cargado. */
    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;

    /** Usuario que confirmó la carga. */
    @Column(name = "usuario_id")
    private Long usuarioId;

    /** Fecha y hora. */
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    /** Total de filas del archivo. */
    @Column(name = "total_filas", nullable = false)
    private Integer totalFilas;

    /** Filas importadas. */
    @Column(name = "filas_validas", nullable = false)
    private Integer filasValidas;

    /** Filas rechazadas. */
    @Column(name = "filas_rechazadas", nullable = false)
    private Integer filasRechazadas;
}
