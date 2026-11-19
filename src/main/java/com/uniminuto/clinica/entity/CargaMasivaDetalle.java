package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Resultado de una fila de una carga masiva (importada o rechazada con su motivo).
 */
@Entity
@Table(name = "carga_masiva_detalle")
@Data
public class CargaMasivaDetalle {

    /** Identificador. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Carga a la que pertenece. */
    @Column(name = "carga_id", nullable = false)
    private Long cargaId;

    /** Número de fila en el archivo (la primera de datos es la 2). */
    @Column(name = "fila", nullable = false)
    private Integer fila;

    /** Código del producto de la fila. */
    @Column(name = "codigo")
    private String codigo;

    /** IMPORTADA o RECHAZADA. */
    @Column(name = "resultado", nullable = false, length = 15)
    private String resultado;

    /** Motivo del rechazo. */
    @Column(name = "motivo", length = 500)
    private String motivo;
}
