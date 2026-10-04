package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.repository.RazaRepository;
import com.uniminuto.clinica.service.RazaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de las operaciones de negocio sobre razas.
 */
@Service
public class RazaServiceImpl implements RazaService {

    /**
     * Repositorio de acceso a la tabla raza.
     */
    @Autowired
    private RazaRepository razaRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Raza> obtenerRazas() {
        return razaRepository.findAll();
    }
}
