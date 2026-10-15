package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.service.MedicamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de las operaciones de negocio sobre medicamentos del inventario.
 */
@Service
public class MedicamentoServiceImpl implements MedicamentoService {

    /**
     * Repositorio de acceso a la tabla medicamento.
     */
    @Autowired
    private MedicamentoRepository medicamentoRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Medicamento> obtenerMedicamentos() {
        return medicamentoRepository.findAll();
    }
}
