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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de {@link CitaService} que centraliza la lógica y las reglas de negocio
 * para la gestión de citas médicas de la clínica veterinaria.
 */
@Service
public class CitaServiceImpl implements CitaService {

    /**
     * Lista de estados válidos permitidos para una cita según la regla RN-CIT-07.
     */
    private static final List<String> ESTADOS_PERMITIDOS = Arrays.asList(
            "Pendiente", "Programada", "Confirmada", "Atendida", "Cancelada"
    );

    /**
     * Repositorio JPA para el acceso a datos de citas.
     */
    @Autowired
    private CitaRepository citaRepository;

    /**
     * Repositorio JPA para validar y obtener clientes.
     */
    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Repositorio JPA para validar y obtener mascotas.
     */
    @Autowired
    private MascotaRepository mascotaRepository;

    /**
     * Repositorio JPA para validar y obtener médicos veterinarios.
     */
    @Autowired
    private MedicoRepository medicoRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Cita> listarCitasPorFecha(LocalDateTime fechaInicial, LocalDateTime fechaFinal)
            throws BadRequestException {
        // Si no se especifican fechas, se listan todas las citas ordenadas descendentemente por fecha
        if (fechaInicial == null && fechaFinal == null) {
            return this.citaRepository.findAllByOrderByFechaHoraDesc();
        }

        if (fechaInicial == null || fechaFinal == null) {
            throw new BadRequestException("Debe proporcionar tanto la fecha inicial como la final para filtrar por rango.");
        }

        if (fechaInicial.isAfter(fechaFinal)) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la fecha final.");
        }

        return this.citaRepository.findAllByFechaHoraBetweenOrderByFechaHoraDesc(fechaInicial, fechaFinal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cita consultarCita(Long citaId) throws BadRequestException {
        if (citaId == null || citaId <= 0) {
            throw new BadRequestException("El identificador de la cita no puede ser nulo o menor o igual a cero.");
        }
        return this.citaRepository.findById(citaId)
                .orElseThrow(() -> new BadRequestException("La cita médica con id " + citaId + " no existe."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Cita> listarCitasAtendidasPorMascota(Integer mascotaId) throws BadRequestException {
        if (mascotaId == null || mascotaId <= 0) {
            throw new BadRequestException("El identificador de la mascota no puede ser nulo o menor o igual a cero.");
        }
        Mascota mascota = this.obtenerMascotaValida(mascotaId);
        return this.citaRepository.findAllByMascota_MascotaIdAndEstadoIgnoreCaseOrderByFechaHoraDesc(
                mascota.getMascotaId(), "atendida");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS guardarCita(CitaRq citaRq) throws BadRequestException {
        this.validarCitaRq(citaRq, true);

        Cliente cliente = this.obtenerClienteValido(citaRq.getClienteId());
        Mascota mascota = this.obtenerMascotaValida(citaRq.getMascotaId());
        Medico medico = this.obtenerMedicoValido(citaRq.getMedicoId());

        // RN-CIT-02: Validar que la mascota pertenezca al cliente seleccionado
        this.validarPertenenciaMascotaCliente(mascota, cliente);

        // RN-CIT-04: Validar no solapamiento del veterinario (+/- 15 minutos)
        this.validarDisponibilidadMedico(medico.getId(), citaRq.getFechaHora(), null);

        // RN-CIT-05: Validar que la mascota no tenga dos citas simultáneas
        this.validarDisponibilidadMascota(mascota.getMascotaId(), citaRq.getFechaHora(), null);

        Cita cita = new Cita();
        cita.setCliente(cliente);
        cita.setMascota(mascota);
        cita.setMedico(medico);
        cita.setFechaHora(citaRq.getFechaHora());
        cita.setEstado(this.normalizarEstado(citaRq.getEstado()));
        cita.setMotivo(citaRq.getMotivo());

        this.citaRepository.save(cita);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Cita médica registrada exitosamente.");
        return respuesta;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS actualizarCita(CitaRq citaRq) throws BadRequestException {
        this.validarCitaRq(citaRq, false);

        if (citaRq.getCitaId() == null || citaRq.getCitaId() <= 0) {
            throw new BadRequestException("El identificador de la cita a actualizar es obligatorio.");
        }

        Cita citaExistente = this.citaRepository.findById(citaRq.getCitaId())
                .orElseThrow(() -> new BadRequestException("La cita a actualizar no existe."));

        // RN-CIT-08: Una cita cancelada no puede ser modificada como si continuara activa
        if ("Cancelada".equalsIgnoreCase(citaExistente.getEstado())) {
            throw new BadRequestException("Una cita cancelada no puede ser modificada como si continuara activa.");
        }

        // RN-CIT-09: Una cita atendida no permite modificar arbitrariamente su información histórica
        if ("Atendida".equalsIgnoreCase(citaExistente.getEstado())) {
            boolean cambioCliente = !citaExistente.getCliente().getClienteId().equals(citaRq.getClienteId());
            boolean cambioMascota = !citaExistente.getMascota().getMascotaId().equals(citaRq.getMascotaId());
            boolean cambioMedico = !citaExistente.getMedico().getId().equals(citaRq.getMedicoId());
            boolean cambioFecha = !citaExistente.getFechaHora().isEqual(citaRq.getFechaHora());

            if (cambioCliente || cambioMascota || cambioMedico || cambioFecha) {
                throw new BadRequestException("Una cita atendida no permite modificar su información histórica (cliente, mascota, médico o fecha/hora).");
            }
        }

        Cliente cliente = this.obtenerClienteValido(citaRq.getClienteId());
        Mascota mascota = this.obtenerMascotaValida(citaRq.getMascotaId());
        Medico medico = this.obtenerMedicoValido(citaRq.getMedicoId());

        // RN-CIT-02: Validar que la mascota pertenezca al cliente
        this.validarPertenenciaMascotaCliente(mascota, cliente);

        // Si la cita no estaba atendida y se modificó la fecha/hora o médico, validar disponibilidad
        if (!"Atendida".equalsIgnoreCase(citaExistente.getEstado())) {
            this.validarDisponibilidadMedico(medico.getId(), citaRq.getFechaHora(), citaExistente.getId());
            this.validarDisponibilidadMascota(mascota.getMascotaId(), citaRq.getFechaHora(), citaExistente.getId());
        }

        citaExistente.setCliente(cliente);
        citaExistente.setMascota(mascota);
        citaExistente.setMedico(medico);
        citaExistente.setFechaHora(citaRq.getFechaHora());
        citaExistente.setEstado(this.normalizarEstado(citaRq.getEstado()));
        citaExistente.setMotivo(citaRq.getMotivo());

        this.citaRepository.save(citaExistente);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Cita médica actualizada exitosamente.");
        return respuesta;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS cancelarCita(Long citaId) throws BadRequestException {
        if (citaId == null || citaId <= 0) {
            throw new BadRequestException("El identificador de la cita es obligatorio.");
        }

        Cita cita = this.citaRepository.findById(citaId)
                .orElseThrow(() -> new BadRequestException("La cita con id " + citaId + " no existe."));

        if ("Cancelada".equalsIgnoreCase(cita.getEstado())) {
            throw new BadRequestException("La cita ya se encuentra en estado Cancelada.");
        }

        if ("Atendida".equalsIgnoreCase(cita.getEstado())) {
            throw new BadRequestException("No es posible cancelar una cita que ya fue atendida.");
        }

        cita.setEstado("Cancelada");
        this.citaRepository.save(cita);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Cita cancelada exitosamente.");
        return respuesta;
    }

    /**
     * Valida los datos estructurales comunes del request de cita.
     *
     * @param citaRq    datos a validar.
     * @param esCreacion indica si la validación corresponde a un registro nuevo.
     * @throws BadRequestException si algún dato requerido es inválido o se ingresa una fecha pasada.
     */
    private void validarCitaRq(CitaRq citaRq, boolean esCreacion) throws BadRequestException {
        if (citaRq == null) {
            throw new BadRequestException("El objeto de solicitud de cita no puede ser nulo.");
        }
        if (citaRq.getClienteId() == null || citaRq.getClienteId() <= 0) {
            throw new BadRequestException("El id del cliente es obligatorio y debe ser mayor a cero.");
        }
        if (citaRq.getMascotaId() == null || citaRq.getMascotaId() <= 0) {
            throw new BadRequestException("El id de la mascota es obligatorio y debe ser mayor a cero.");
        }
        if (citaRq.getMedicoId() == null || citaRq.getMedicoId() <= 0) {
            throw new BadRequestException("El id del médico veterinario es obligatorio y debe ser mayor a cero.");
        }
        if (citaRq.getFechaHora() == null) {
            throw new BadRequestException("La fecha y hora de la cita es obligatoria.");
        }
        if (citaRq.getEstado() == null || citaRq.getEstado().trim().isEmpty()) {
            throw new BadRequestException("El estado de la cita es obligatorio.");
        }

        // RN-CIT-07: Validar que el estado pertenezca a los permitidos
        boolean estadoValido = ESTADOS_PERMITIDOS.stream()
                .anyMatch(e -> e.equalsIgnoreCase(citaRq.getEstado().trim()));
        if (!estadoValido) {
            throw new BadRequestException("El estado '" + citaRq.getEstado() + "' no es válido. Estados permitidos: " + ESTADOS_PERMITIDOS);
        }

        // RN-CIT-06: No se pueden registrar citas en fechas/horas pasadas
        if (esCreacion && citaRq.getFechaHora().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("No se pueden registrar citas en fechas u horas anteriores a la actual.");
        }
    }

    /**
     * RN-CIT-02: Valida que la mascota indicada pertenezca efectivamente al cliente que solicita la cita.
     *
     * @param mascota entidad mascota obtenida de la BD.
     * @param cliente entidad cliente obtenida de la BD.
     * @throws BadRequestException si la mascota no pertenece al cliente.
     */
    private void validarPertenenciaMascotaCliente(Mascota mascota, Cliente cliente) throws BadRequestException {
        if (mascota.getCliente() == null || !mascota.getCliente().getClienteId().equals(cliente.getClienteId())) {
            throw new BadRequestException("La mascota '" + mascota.getNombreMascota() + "' no pertenece al cliente seleccionado ("
                    + cliente.getNombres() + " " + cliente.getApellidos() + ").");
        }
    }

    /**
     * RN-CIT-04: Valida que el médico veterinario no tenga otra cita asignada en un intervalo
     * de 15 minutos antes o 15 minutos después.
     *
     * @param medicoId      identificador del médico.
     * @param fechaHora     fecha y hora deseada.
     * @param idCitaExcluir identificador de la cita a excluir (para casos de actualización).
     * @throws BadRequestException si existe conflicto horario.
     */
    private void validarDisponibilidadMedico(Long medicoId, LocalDateTime fechaHora, Long idCitaExcluir) throws BadRequestException {
        LocalDateTime inicioRango = fechaHora.minusMinutes(15);
        LocalDateTime finRango = fechaHora.plusMinutes(15);

        List<Cita> citasConflicto = this.citaRepository.findAllByMedico_IdAndFechaHoraBetween(medicoId, inicioRango, finRango);

        boolean hayConflicto = citasConflicto.stream()
                .filter(c -> idCitaExcluir == null || !c.getId().equals(idCitaExcluir))
                .anyMatch(c -> !("Cancelada".equalsIgnoreCase(c.getEstado())));

        if (hayConflicto) {
            throw new BadRequestException("El médico veterinario no está disponible en el horario seleccionado. "
                    + "Ya cuenta con una cita en un intervalo menor a 15 minutos antes o después.");
        }
    }

    /**
     * RN-CIT-05: Valida que la mascota no tenga dos citas asignadas exactamente en la misma fecha y hora.
     *
     * @param mascotaId     identificador de la mascota.
     * @param fechaHora     fecha y hora de la cita.
     * @param idCitaExcluir identificador de la cita actual a excluir al actualizar.
     * @throws BadRequestException si la mascota ya tiene otra cita activa a la misma hora.
     */
    private void validarDisponibilidadMascota(Integer mascotaId, LocalDateTime fechaHora, Long idCitaExcluir) throws BadRequestException {
        List<Cita> citasMascota = this.citaRepository.findAllByMascota_MascotaIdAndFechaHora(mascotaId, fechaHora);

        boolean hayConflicto = citasMascota.stream()
                .filter(c -> idCitaExcluir == null || !c.getId().equals(idCitaExcluir))
                .anyMatch(c -> !("Cancelada".equalsIgnoreCase(c.getEstado())));

        if (hayConflicto) {
            throw new BadRequestException("La mascota ya tiene otra cita activa programada en la misma fecha y hora.");
        }
    }

    /**
     * Normaliza el texto del estado para almacenarlo con formato Capitalizado uniforme.
     *
     * @param estado texto del estado.
     * @return estado normalizado.
     */
    private String normalizarEstado(String estado) {
        if (estado == null) return "Pendiente";
        return ESTADOS_PERMITIDOS.stream()
                .filter(e -> e.equalsIgnoreCase(estado.trim()))
                .findFirst()
                .orElse(estado.trim());
    }

    /**
     * Obtiene el cliente indicado o lanza excepción 400 si no existe.
     */
    private Cliente obtenerClienteValido(Long clienteId) throws BadRequestException {
        return this.clienteRepository.findById(clienteId)
                .orElseThrow(() -> new BadRequestException("El cliente con id " + clienteId + " no existe."));
    }

    /**
     * Obtiene la mascota indicada o lanza excepción 400 si no existe.
     */
    private Mascota obtenerMascotaValida(Integer mascotaId) throws BadRequestException {
        return this.mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new BadRequestException("La mascota con id " + mascotaId + " no existe."));
    }

    /**
     * Obtiene el médico indicado o lanza excepción 400 si no existe.
     */
    private Medico obtenerMedicoValido(Long medicoId) throws BadRequestException {
        return this.medicoRepository.findById(medicoId)
                .orElseThrow(() -> new BadRequestException("El médico veterinario con id " + medicoId + " no existe."));
    }
}
