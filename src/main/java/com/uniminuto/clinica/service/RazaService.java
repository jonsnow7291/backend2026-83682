package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Raza;

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
}
