package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.ClienteApi;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.service.ClienteService;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementación REST de {@link ClienteApi}, delega la lógica de negocio en {@link ClienteService}.
 */
@RestController
public class ClienteApiController implements ClienteApi {

    /**
     * Servicio de negocio para operaciones sobre clientes.
     */
    @Autowired
    private ClienteService clienteService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<Cliente>> listarClientes() throws BadRequestException {
        return ResponseEntity.ok(this.clienteService.obtenerClientes());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Cliente> consultarCliente(Long clienteId) throws BadRequestException {
        return ResponseEntity.ok(this.clienteService.consultarCliente(clienteId));
    }
}
