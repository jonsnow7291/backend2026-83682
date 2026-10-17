package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;

/**
 * Entidad JPA que representa un cliente (propietario de mascotas) de la clínica.
 * Mapea la tabla {@code cliente}.
 */
@Entity
@Table(name = "cliente")
@Data
public class Cliente {

    /**
     * Identificador único del cliente, autogenerado por la base de datos.
     */
    @Id
    @Column(name = "cliente_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clienteId;

    /**
     * Identificador del usuario del sistema asociado al cliente.
     */
    @Column(name = "usuario_id")
    private Integer usuarioId;

    /**
     * Tipo de documento de identidad (ej. CC, TI, CE).
     */
    @Column(name = "tipo_documento", nullable = false, length = 10)
    private String tipoDocumento;

    /**
     * Número de documento de identidad (único).
     */
    @Column(name = "numero_documento", nullable = false, unique = true, length = 20)
    private String numeroDocumento;

    /**
     * Nombres del cliente.
     */
    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    /**
     * Apellidos del cliente.
     */
    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    /**
     * Fecha de nacimiento del cliente.
     */
    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    /**
     * Género del cliente (M o F).
     */
    @Column(name = "genero", length = 1)
    private String genero;

    /**
     * Teléfono de contacto.
     */
    @Column(name = "telefono", length = 20)
    private String telefono;

    /**
     * Dirección de residencia.
     */
    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;

    /**
     * Indica si el cliente está activo.
     */
    @Column(name = "activo", nullable = false)
    private Boolean activo;
}
