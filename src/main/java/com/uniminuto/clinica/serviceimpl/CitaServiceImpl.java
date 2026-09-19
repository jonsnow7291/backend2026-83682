package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de {@link CitaService} usando Spring Data JPA.
 */
@Service
public class CitaServiceImpl implements CitaService {

    /**
     * Repositorio JPA para el acceso a datos de citas.
     */
    @Autowired
    private CitaRepository citaRepository;

    /**
     * Repositorio JPA para el acceso a datos de clientes, usado para validar referencias.
     */
    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Repositorio JPA para el acceso a datos de mascotas, usado para validar referencias.
     */
    @Autowired
    private MascotaRepository mascotaRepository;

    /**
     * Repositorio JPA para el acceso a datos de médicos, usado para validar referencias.
     */
    @Autowired
    private MedicoRepository medicoRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Cita> listarCitasPorFecha(LocalDateTime fechaInicial, LocalDateTime fechaFinal)
            throws BadRequestException {
        if (fechaInicial == null || fechaFinal == null) {
            throw new BadRequestException("Las fechas inicial y final no pueden ser nulas");
        }
        if (fechaInicial.isAfter(fechaFinal)) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la fecha final");
        }
        return this.citaRepository.findAllByFechaHoraBetweenOrderByFechaHoraDesc(fechaInicial, fechaFinal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS guardarCita(CitaRq citaRq) throws BadRequestException {
        this.validarCitaRq(citaRq);

        Cliente cliente = this.obtenerClienteValido(citaRq.getClienteId());
        Mascota mascota = this.obtenerMascotaValida(citaRq.getMascotaId());
        Medico medico = this.obtenerMedicoValido(citaRq.getMedicoId());

        Cita cita = new Cita();
        cita.setCliente(cliente);
        cita.setMascota(mascota);
        cita.setMedico(medico);
        cita.setFechaHora(citaRq.getFechaHora());
        cita.setEstado(citaRq.getEstado());
        cita.setMotivo(citaRq.getMotivo());

        this.citaRepository.save(cita);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Cita guardada exitosamente");
        return respuesta;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS actualizarCita(CitaRq citaRq) throws BadRequestException {
        this.validarCitaRq(citaRq);

        if (citaRq.getCitaId() == null || citaRq.getCitaId() <= 0) {
            throw new BadRequestException("El id de la cita no puede ser nulo o negativo");
        }

        Optional<Cita> optCita = this.citaRepository.findById(citaRq.getCitaId());
        if (optCita.isEmpty()) {
            throw new BadRequestException("La cita seleccionada no es valida");
        }

        Cliente cliente = this.obtenerClienteValido(citaRq.getClienteId());
        Mascota mascota = this.obtenerMascotaValida(citaRq.getMascotaId());
        Medico medico = this.obtenerMedicoValido(citaRq.getMedicoId());

        Cita citaExistente = optCita.get();
        citaExistente.setCliente(cliente);
        citaExistente.setMascota(mascota);
        citaExistente.setMedico(medico);
        citaExistente.setFechaHora(citaRq.getFechaHora());
        citaExistente.setEstado(citaRq.getEstado());
        citaExistente.setMotivo(citaRq.getMotivo());

        this.citaRepository.save(citaExistente);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Cita actualizada exitosamente");
        return respuesta;
    }

    /**
     * Valida que los datos comunes de creación/actualización de una cita sean correctos.
     *
     * @param citaRq datos a validar.
     * @throws BadRequestException si algún dato es nulo, vacío o inválido.
     */
    private void validarCitaRq(CitaRq citaRq) throws BadRequestException {
        if (citaRq == null) {
            throw new BadRequestException("El objeto CitaRq no puede ser nulo");
        }
        if (citaRq.getClienteId() == null || citaRq.getClienteId() <= 0) {
            throw new BadRequestException("El id del cliente no puede ser nulo o negativo");
        }
        if (citaRq.getMascotaId() == null || citaRq.getMascotaId() <= 0) {
            throw new BadRequestException("El id de la mascota no puede ser nulo o negativo");
        }
        if (citaRq.getMedicoId() == null || citaRq.getMedicoId() <= 0) {
            throw new BadRequestException("El id del medico no puede ser nulo o negativo");
        }
        if (citaRq.getFechaHora() == null) {
            throw new BadRequestException("La fecha y hora de la cita no puede ser nula");
        }
        if (citaRq.getEstado() == null || citaRq.getEstado().isEmpty()) {
            throw new BadRequestException("El estado de la cita no puede ser nulo o vacío");
        }
    }

    /**
     * Obtiene el cliente indicado o lanza una excepción si no existe.
     *
     * @param clienteId identificador del cliente.
     * @return el cliente encontrado.
     * @throws BadRequestException si el cliente no existe.
     */
    private Cliente obtenerClienteValido(Long clienteId) throws BadRequestException {
        return this.clienteRepository.findById(clienteId)
                .orElseThrow(() -> new BadRequestException("El cliente seleccionado no es valido"));
    }

    /**
     * Obtiene la mascota indicada o lanza una excepción si no existe.
     *
     * @param mascotaId identificador de la mascota.
     * @return la mascota encontrada.
     * @throws BadRequestException si la mascota no existe.
     */
    private Mascota obtenerMascotaValida(Integer mascotaId) throws BadRequestException {
        return this.mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new BadRequestException("La mascota seleccionada no es valida"));
    }

    /**
     * Obtiene el médico indicado o lanza una excepción si no existe.
     *
     * @param medicoId identificador del médico.
     * @return el médico encontrado.
     * @throws BadRequestException si el médico no existe.
     */
    private Medico obtenerMedicoValido(Long medicoId) throws BadRequestException {
        return this.medicoRepository.findById(medicoId)
                .orElseThrow(() -> new BadRequestException("El medico seleccionado no es valido"));
    }
}
