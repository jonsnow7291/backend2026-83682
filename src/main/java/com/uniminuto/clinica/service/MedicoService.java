package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;

import com.uniminuto.clinica.models.MedicoRq;

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

    /** Registra un médico nuevo. */
    Medico guardarMedico(MedicoRq rq);

    /** Actualiza un médico existente. */
    Medico actualizarMedico(MedicoRq rq);

    /** Crea una cuenta de acceso (rol MEDICO) vinculada a un médico existente sin usuario. */
    MiRespuestaRS crearAccesoUsuario(Long id) throws BadRequestException;

    /** Elimina un médico (y su usuario asociado si existe). Solo ADMIN. */
    void eliminarMedico(Long id) throws BadRequestException;
}
