package com.uniminuto.clinica.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un usuario del sistema.
 */
@Entity
@Table(name = "usuario")
@Data
public class Usuario {

    /**
     * Identificador del usuario.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre de usuario único.
     */
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    /**
     * Hash de la contraseña. Se excluye de las respuestas JSON para no exponerlo.
     */
    @JsonIgnore
    @Column(name = "password_hash", columnDefinition = "TEXT")
    private String passwordHash;

    /**
     * Rol del usuario (ej. ADMIN).
     */
    @Column(name = "rol", nullable = false, length = 30)
    private String rol;

    /**
     * Fecha de creación del usuario.
     */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    /**
     * Indica si el usuario está activo.
     */
    @Column(name = "activo")
    private Boolean activo;

    /**
     * Correo electrónico del usuario.
     */
    @Column(name = "email", nullable = false)
    private String email;
}
