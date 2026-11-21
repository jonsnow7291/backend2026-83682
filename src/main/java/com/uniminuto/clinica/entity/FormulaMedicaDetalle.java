package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Línea de una fórmula: medicamento, dosis, frecuencia, duración y cantidad prescrita.
 */
@Entity
@Table(name = "formula_medica_detalle")
@Data
public class FormulaMedicaDetalle {

    /** Identificador. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Medicamento prescrito. */
    @ManyToOne
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    /** Dosis. */
    @Column(name = "dosis", nullable = false)
    private String dosis;

    /** Frecuencia de administración. */
    @Column(name = "frecuencia", nullable = false, length = 100)
    private String frecuencia;

    /** Duración del tratamiento en días. */
    @Column(name = "duracion_dias", nullable = false)
    private Integer duracionDias;

    /** Unidades que se administran en cada toma (opcional; permite calcular la cantidad total). */
    @Column(name = "unidades_por_dosis")
    private Integer unidadesPorDosis;

    /** Tomas por día (opcional; permite calcular la cantidad total). */
    @Column(name = "dosis_por_dia")
    private Integer dosisPorDia;

    /** Cantidad prescrita (unidades a despachar). */
    @Column(name = "cantidad_prescrita", nullable = false)
    private Integer cantidadPrescrita;

    /** Vía de administración. */
    @Column(name = "via_administracion", length = 50)
    private String viaAdministracion;

    /** Indicaciones de la línea. */
    @Column(name = "indicaciones", columnDefinition = "TEXT")
    private String indicaciones;
}
