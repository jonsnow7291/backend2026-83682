package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Usuario;

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
}
