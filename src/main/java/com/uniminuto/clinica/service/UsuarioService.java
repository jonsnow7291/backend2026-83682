package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Usuario;

import com.uniminuto.clinica.models.UsuarioRq;

import java.util.List;

/**
 * Operaciones de negocio sobre usuarios del sistema.
 */
public interface UsuarioService {

    /**
     * Obtiene todos los registros de usuarios del sistema.
     *
     * @return lista completa.
     */
    List<Usuario> obtenerUsuarios();

    /** Crea un usuario con contraseña cifrada (BCrypt). */
    Usuario guardarUsuario(UsuarioRq rq);

    /** Actualiza correo, rol y (opcionalmente) contraseña. */
    Usuario actualizarUsuario(UsuarioRq rq);

    /** Activa o inactiva un usuario (un administrador no puede inactivarse a sí mismo). */
    Usuario cambiarEstado(Long id, boolean activo);

    /** Elimina un usuario (y su médico asociado si existe). Solo ADMIN. */
    void eliminarUsuario(Long id);
}
