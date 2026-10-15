package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de las operaciones de negocio sobre usuarios del sistema.
 */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    /**
     * Repositorio de acceso a la tabla usuario.
     */
    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Usuario> obtenerUsuarios() {
        return usuarioRepository.findAll();
    }
}
