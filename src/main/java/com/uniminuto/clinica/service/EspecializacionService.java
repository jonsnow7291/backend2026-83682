package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Especializacion;

import java.util.List;

/**
 * Operaciones de negocio sobre especializaciones médicas.
 */
public interface EspecializacionService {

    /**
     * Obtiene todos los registros de especializaciones médicas.
     *
     * @return lista completa.
     */
    List<Especializacion> obtenerEspecializaciones();

    /**
     * Activa o inactiva el registro (no se elimina para conservar el histórico).
     *
     * @param id     identificador.
     * @param activo nuevo estado.
     * @return registro actualizado.
     */
    Especializacion cambiarEstado(Long id, boolean activo);
}
