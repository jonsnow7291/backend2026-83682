package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.HorarioVeterinario;
import com.uniminuto.clinica.models.HorarioRq;

import java.util.List;

/**
 * Gestión de los horarios de disponibilidad de los veterinarios.
 */
public interface HorarioService {

    /**
     * @param medicoId médico.
     * @return horarios del médico.
     */
    List<HorarioVeterinario> listar(Long medicoId);

    /**
     * Crea una franja (valida que no se cruce con otra del mismo día).
     *
     * @param rq datos.
     * @return franja creada.
     */
    HorarioVeterinario crear(HorarioRq rq);

    /**
     * Actualiza una franja.
     *
     * @param rq datos con id.
     * @return franja actualizada.
     */
    HorarioVeterinario actualizar(HorarioRq rq);

    /**
     * Elimina una franja de horario (no afecta citas ya agendadas).
     *
     * @param id franja.
     */
    void eliminar(Long id);
}
