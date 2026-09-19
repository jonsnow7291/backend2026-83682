package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Entidad JPA que representa un médico veterinario de la clínica.
 * Mapea la tabla {@code medico}. Solo se utiliza para validar referencias
 * (citas, anotaciones de historia) ya que su gestión (CRUD) no forma parte del alcance actual.
 */
@Entity
@Table(name = "medico")
@Data
public class Medico {

    /**
     * Identificador único del médico, autogenerado por la base de datos.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tipo de documento de identidad del médico (ej. CC, TI, CE).
     */
    @Column(name = "tipo_documento", nullable = false, length = 10)
    private String tipoDocumento;

    /**
     * Número de documento de identidad del médico.
     */
    @Column(name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento;

    /**
     * Nombres del médico.
     */
    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    /**
     * Apellidos del médico.
     */
    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    /**
     * Número de teléfono de contacto del médico.
     */
    @Column(name = "telefono", length = 20)
    private String telefono;

    /**
     * Número de registro profesional del médico.
     */
    @Column(name = "registro_profesional", nullable = false, length = 50)
    private String registroProfesional;

    /**
     * Identificador de la especialización del médico.
     */
    @Column(name = "especializacion_id", nullable = false)
    private Integer especializacionId;
}
