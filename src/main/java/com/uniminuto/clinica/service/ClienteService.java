package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;

import java.util.List;

/**
 * Servicio de negocio para la consulta de clientes.
 */
public interface ClienteService {

    /**
     * Obtiene todos los clientes registrados.
     *
     * @return lista completa de clientes.
     */
    List<Cliente> obtenerClientes();

    /**
     * Consulta un cliente específico por su identificador.
     *
     * @param clienteId identificador del cliente.
     * @return el cliente encontrado.
     * @throws BadRequestException si el identificador es inválido o el cliente no existe.
     */
    Cliente consultarCliente(Long clienteId) throws BadRequestException;
}
