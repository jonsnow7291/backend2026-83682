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

    /**
     * Registra una nueva especialidad (requerimiento 3.1).
     *
     * @param rq datos de la especialidad.
     * @return la especialidad creada (activa).
     */
    Especializacion guardar(com.uniminuto.clinica.models.EspecializacionRq rq);

    /**
     * Edita nombre, descripción y código de una especialidad.
     *
     * @param rq datos con id.
     * @return la especialidad actualizada.
     */
    Especializacion actualizar(com.uniminuto.clinica.models.EspecializacionRq rq);
}
