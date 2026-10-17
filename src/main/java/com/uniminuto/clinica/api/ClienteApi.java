package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Contrato REST para la consulta de clientes de la clínica veterinaria.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cliente")
public interface ClienteApi {

    /**
     * Lista todos los clientes registrados.
     *
     * @return respuesta HTTP con la lista completa de clientes.
     * @throws BadRequestException si ocurre un error al procesar la solicitud.
     */
    @GetMapping(value = "/listar-todos",
            produces = {"application/json"})
    ResponseEntity<List<Cliente>> listarClientes()
            throws BadRequestException;

    /**
     * Consulta un cliente específico por su identificador.
     *
     * @param clienteId identificador del cliente.
     * @return respuesta HTTP con el cliente encontrado.
     * @throws BadRequestException si el identificador es inválido o el cliente no existe.
     */
    @GetMapping(value = "/consultar",
            produces = {"application/json"})
    ResponseEntity<Cliente> consultarCliente(
            @RequestParam Long clienteId)
            throws BadRequestException;
}
