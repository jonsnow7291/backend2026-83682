package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.ClienteRq;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Implementación de {@link ClienteService}.
 */
@Service
public class ClienteServiceImpl implements ClienteService {

    private static final Set<String> TIPOS_DOCUMENTO = Set.of("CC", "CE", "TI", "PA", "NIT");
    private static final Pattern DOCUMENTO = Pattern.compile("[A-Za-z0-9]{5,20}");
    private static final Pattern TELEFONO = Pattern.compile("[0-9+\\s-]{7,20}");
    private static final Pattern SIN_MARCAS = Pattern.compile("[^<>]*");

    @Autowired
    private ClienteRepository clienteRepository;

    @Override
    public List<Cliente> obtenerClientes() {
        return clienteRepository.findAll();
    }

    @Override
    public Cliente consultarCliente(Long clienteId) throws BadRequestException {
        if (clienteId == null || clienteId <= 0) {
            throw new BadRequestException("El id del cliente no puede ser nulo o negativo");
        }
        return this.clienteRepository.findById(clienteId)
                .orElseThrow(() -> new NotFoundException("El cliente " + clienteId + " no existe"));
    }

    @Override
    public Cliente guardarCliente(ClienteRq rq) {
        validar(rq);
        validarDocumentoUnico(rq, null);
        Cliente c = new Cliente();
        aplicar(c, rq);
        c.setActivo(rq.getActivo() == null ? Boolean.TRUE : rq.getActivo());
        return clienteRepository.save(c);
    }

    @Override
    public Cliente actualizarCliente(ClienteRq rq) {
        if (rq == null || rq.getClienteId() == null || rq.getClienteId() <= 0) {
            throw new BadRequestException("El id del cliente es obligatorio");
        }
        validar(rq);
        Cliente c = clienteRepository.findById(rq.getClienteId())
                .orElseThrow(() -> new NotFoundException("El cliente " + rq.getClienteId() + " no existe"));
        validarDocumentoUnico(rq, c.getClienteId());
        aplicar(c, rq);
        if (rq.getActivo() != null) {
            c.setActivo(rq.getActivo());
        }
        return clienteRepository.save(c);
    }

    private void validar(ClienteRq rq) {
        if (rq == null) {
            throw new BadRequestException("Los datos del cliente son obligatorios");
        }
        String tipo = rq.getTipoDocumento() == null ? "" : rq.getTipoDocumento().trim().toUpperCase();
        if (!TIPOS_DOCUMENTO.contains(tipo)) {
            throw new BadRequestException("El tipo de documento debe ser uno de " + TIPOS_DOCUMENTO);
        }
        if (rq.getNumeroDocumento() == null || !DOCUMENTO.matcher(rq.getNumeroDocumento().trim()).matches()) {
            throw new BadRequestException("El número de documento debe tener entre 5 y 20 caracteres alfanuméricos");
        }
        texto(rq.getNombres(), "Los nombres", 100, true);
        texto(rq.getApellidos(), "Los apellidos", 100, true);
        if (rq.getFechaNacimiento() == null || !rq.getFechaNacimiento().isBefore(LocalDate.now())
                || rq.getFechaNacimiento().isBefore(LocalDate.now().minusYears(120))) {
            throw new BadRequestException("La fecha de nacimiento es obligatoria y debe ser una fecha pasada válida");
        }
        if (rq.getGenero() != null && !rq.getGenero().isBlank()
                && !Set.of("M", "F", "O").contains(rq.getGenero().trim().toUpperCase())) {
            throw new BadRequestException("El género debe ser M, F u O");
        }
        if (rq.getTelefono() != null && !rq.getTelefono().isBlank()
                && !TELEFONO.matcher(rq.getTelefono().trim()).matches()) {
            throw new BadRequestException("El teléfono debe tener entre 7 y 20 dígitos");
        }
        texto(rq.getDireccion(), "La dirección", 255, false);
    }

    private void texto(String valor, String campo, int max, boolean obligatorio) {
        if (valor == null || valor.isBlank()) {
            if (obligatorio) {
                throw new BadRequestException("Campo obligatorio: " + campo.toLowerCase());
            }
            return;
        }
        if (valor.trim().length() > max) {
            throw new BadRequestException(campo + " no pueden superar " + max + " caracteres");
        }
        if (!SIN_MARCAS.matcher(valor).matches()) {
            throw new BadRequestException(campo + " contienen caracteres no permitidos (< >)");
        }
    }

    private void validarDocumentoUnico(ClienteRq rq, Long idExcluir) {
        String doc = rq.getNumeroDocumento().trim();
        boolean existe = clienteRepository.findAll().stream()
                .anyMatch(c -> c.getNumeroDocumento().equalsIgnoreCase(doc) && !c.getClienteId().equals(idExcluir));
        if (existe) {
            throw new ConflictException("Ya existe un cliente con el documento " + doc);
        }
    }

    private void aplicar(Cliente c, ClienteRq rq) {
        c.setTipoDocumento(rq.getTipoDocumento().trim().toUpperCase());
        c.setNumeroDocumento(rq.getNumeroDocumento().trim());
        c.setNombres(rq.getNombres().trim());
        c.setApellidos(rq.getApellidos().trim());
        c.setFechaNacimiento(rq.getFechaNacimiento());
        c.setGenero(rq.getGenero() == null || rq.getGenero().isBlank() ? null : rq.getGenero().trim().toUpperCase());
        c.setTelefono(rq.getTelefono() == null || rq.getTelefono().isBlank() ? null : rq.getTelefono().trim());
        c.setDireccion(rq.getDireccion() == null || rq.getDireccion().isBlank() ? null : rq.getDireccion().trim());
    }
}
