package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Medicamento;

import java.util.List;

/**
 * Operaciones de negocio sobre medicamentos del inventario.
 */
public interface MedicamentoService {

    /**
     * Obtiene todos los registros de medicamentos del inventario.
     *
     * @return lista completa.
     */
    List<Medicamento> obtenerMedicamentos();
}
