package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.repository.EspecializacionRepository;
import com.uniminuto.clinica.service.EspecializacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de las operaciones de negocio sobre especializaciones médicas.
 */
@Service
public class EspecializacionServiceImpl implements EspecializacionService {

    /**
     * Repositorio de acceso a la tabla especializacion.
     */
    @Autowired
    private EspecializacionRepository especializacionRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Especializacion> obtenerEspecializaciones() {
        return especializacionRepository.findAll();
    }
}
