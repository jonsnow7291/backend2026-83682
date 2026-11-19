package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Tarifa;
import com.uniminuto.clinica.entity.TarifaLog;
import com.uniminuto.clinica.models.TarifaRq;

import java.util.List;

/**
 * Gestión de tarifas por especialidad con historial de cambios.
 */
public interface TarifaService {

    /**
     * @param especializacionId filtro opcional.
     * @param todas             {@code true} incluye inactivas.
     * @return tarifas.
     */
    List<Tarifa> listar(Long especializacionId, boolean todas);

    /**
     * Crea una tarifa y registra el log.
     *
     * @param rq datos.
     * @return tarifa creada.
     */
    Tarifa crear(TarifaRq rq);

    /**
     * Modifica una tarifa y registra valor anterior/nuevo, usuario y fecha. Las facturas ya emitidas no cambian.
     *
     * @param rq datos con id.
     * @return tarifa actualizada.
     */
    Tarifa actualizar(TarifaRq rq);

    /**
     * Activa o inactiva una tarifa.
     *
     * @param id     tarifa.
     * @param activa nuevo estado.
     * @return tarifa actualizada.
     */
    Tarifa cambiarEstado(Long id, boolean activa);

    /**
     * @param id tarifa.
     * @return historial de cambios.
     */
    List<TarifaLog> historial(Long id);
}
