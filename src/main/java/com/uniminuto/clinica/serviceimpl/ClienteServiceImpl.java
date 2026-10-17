package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de {@link ClienteService} usando Spring Data JPA.
 */
@Service
public class ClienteServiceImpl implements ClienteService {

    /**
     * Repositorio JPA para el acceso a datos de clientes.
     */
    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Cliente> obtenerClientes() {
        return clienteRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cliente consultarCliente(Long clienteId) throws BadRequestException {
        if (clienteId == null || clienteId <= 0) {
            throw new BadRequestException("El id del cliente no puede ser nulo o negativo");
        }
        return this.clienteRepository.findById(clienteId)
                .orElseThrow(() -> new BadRequestException("El cliente seleccionado no existe"));
    }
}
