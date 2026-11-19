package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.MedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de las operaciones de negocio sobre médicos veterinarios.
 */
@Service
public class MedicoServiceImpl implements MedicoService {

    /**
     * Repositorio de acceso a la tabla medico.
     */
    @Autowired
    private MedicoRepository medicoRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Medico> obtenerMedicos() {
        return medicoRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Medico cambiarEstado(Long id, boolean activo) {
        Medico e = medicoRepository.findById(id)
                .orElseThrow(() -> new com.uniminuto.clinica.exception.NotFoundException("El registro <built-in function id> no existe".replace("{id}", String.valueOf(id))));
        e.setActivo(activo);
        return medicoRepository.save(e);
    }
}
