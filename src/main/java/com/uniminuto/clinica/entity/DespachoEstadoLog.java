package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Historial de cambios de estado de un despacho, con usuario y fecha.
 */
@Entity
@Table(name = "despacho_estado_log")
@Data
public class DespachoEstadoLog {

    /** Identificador. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Despacho. */
    @Column(name = "despacho_id", nullable = false)
    private Long despachoId;

    /** Estado previo. */
    @Column(name = "estado_anterior", length = 30)
    private String estadoAnterior;

    /** Estado nuevo. */
    @Column(name = "estado_nuevo", nullable = false, length = 30)
    private String estadoNuevo;

    /** Usuario que provocó el cambio. */
    @Column(name = "usuario_id")
    private Long usuarioId;

    /** Fecha y hora. */
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    /** Motivo o detalle. */
    @Column(name = "motivo", length = 500)
    private String motivo;
}
