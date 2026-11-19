package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Tarifa de un servicio por especialidad. El administrador la modifica sin tocar código.
 */
@Entity
@Table(name = "tarifa")
@Data
public class Tarifa {

    /** Identificador. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Especialidad a la que aplica. */
    @Column(name = "especializacion_id", nullable = false)
    private Long especializacionId;

    /** Servicio (consulta, vacunación, procedimiento...). */
    @Column(name = "servicio", nullable = false, length = 100)
    private String servicio;

    /** Valor antes de IVA. */
    @Column(name = "valor", nullable = false)
    private BigDecimal valor;

    /** Porcentaje de IVA. */
    @Column(name = "iva_porcentaje", nullable = false)
    private BigDecimal ivaPorcentaje = BigDecimal.ZERO;

    /** Indica si la tarifa está vigente. */
    @Column(name = "activa", nullable = false)
    private Boolean activa = true;
}
