package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Medico;

import java.util.List;

/**
 * Operaciones de negocio sobre médicos veterinarios.
 */
public interface MedicoService {

    /**
     * Obtiene todos los registros de médicos veterinarios.
     *
     * @return lista completa.
     */
    List<Medico> obtenerMedicos();

    /**
     * Activa o inactiva el registro (no se elimina para conservar el histórico).
     *
     * @param id     identificador.
     * @param activo nuevo estado.
     * @return registro actualizado.
     */
    Medico cambiarEstado(Long id, boolean activo);
}
