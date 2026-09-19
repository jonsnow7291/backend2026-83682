package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de {@link FormulaMedicaService} usando Spring Data JPA.
 */
@Service
public class FormulaMedicaServiceImpl implements FormulaMedicaService {

    /**
     * Repositorio JPA para el acceso a datos de fórmulas médicas.
     */
    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FormulaMedica> listarFormulasMedicas() throws BadRequestException {
        return this.formulaMedicaRepository.findAllByOrderByFechaCreacionRegistroDesc();
    }
}
