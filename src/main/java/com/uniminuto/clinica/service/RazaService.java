package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Raza;

import com.uniminuto.clinica.models.RazaRq;

import java.util.List;

/**
 * Operaciones de negocio sobre las razas de mascotas.
 */
public interface RazaService {

    /**
     * Obtiene todas las razas registradas.
     *
     * @return lista de razas.
     */
    List<Raza> obtenerRazas();

    /** Registra una raza nueva (nombre único por especie). */
    Raza guardarRaza(RazaRq rq);

    /** Actualiza una raza existente. */
    Raza actualizarRaza(RazaRq rq);
}
