package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.CitaCalendarioRs;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.ReprogramarRq;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.security.Roles;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.service.CitaService;
import com.uniminuto.clinica.util.CitaEstados;
import com.uniminuto.clinica.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación de la agenda de citas. Reglas de negocio (RN) validadas aquí:
 * RN1 el veterinario no tiene citas solapadas; RN2 la mascota no tiene citas simultáneas;
 * RN3 no se asignan citas a médicos inactivos; RN4 (horario del veterinario) se retiró: se puede
 * agendar a cualquier hora; RN5 las canceladas conservan historial; RN6 la mascota pertenece al propietario;
 * RN7 un MEDICO solo ve y gestiona sus citas.
 */
@Service
public class CitaServiceImpl implements CitaService {

    private static final int DURACION_DEFECTO = 30;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private UsuarioActual usuarioActual;

    /** {@inheritDoc} */
    @Override
    public List<Cita> listarCitasPorFecha(LocalDateTime fechaInicial, LocalDateTime fechaFinal) throws BadRequestException {
        List<Cita> citas;
        if (fechaInicial == null && fechaFinal == null) {
            citas = citaRepository.findAllByOrderByFechaHoraDesc();
        } else {
            if (fechaInicial == null || fechaFinal == null) {
                throw new BadRequestException("Debe proporcionar tanto la fecha inicial como la final para filtrar por rango.");
            }
            if (fechaInicial.isAfter(fechaFinal)) {
                throw new BadRequestException("La fecha inicial no puede ser posterior a la fecha final.");
            }
            citas = citaRepository.findAllByFechaHoraBetweenOrderByFechaHoraDesc(fechaInicial, fechaFinal);
        }
        Long propio = medicoRestringido();
        List<Cita> resultado = propio == null ? citas : citas.stream().filter(c -> c.getMedico().getId().equals(propio)).toList();
        return resultado.stream().filter(this::estaDisponible).toList();
    }

    /** {@inheritDoc} */
    @Override
    public Cita consultarCita(Long citaId) throws BadRequestException {
        if (citaId == null || citaId <= 0) {
            throw new BadRequestException("El identificador de la cita no puede ser nulo o menor o igual a cero.");
        }
        Cita c = citaRepository.findById(citaId)
                .orElseThrow(() -> new NotFoundException("La cita médica con id " + citaId + " no existe."));
        usuarioActualExigirPropia(c);
        if (!estaDisponible(c)) {
            throw new BadRequestException("La cita no está disponible porque cliente o médico están inactivos");
        }
        return c;
    }

    /** {@inheritDoc} */
    @Override
    public List<Cita> listarCitasAtendidasPorMascota(Integer mascotaId) throws BadRequestException {
        if (mascotaId == null || mascotaId <= 0) {
            throw new BadRequestException("El identificador de la mascota no puede ser nulo o menor o igual a cero.");
        }
        obtenerMascota(mascotaId);
        return citaRepository.findAllByMascota_MascotaIdOrderByFechaHoraDesc(mascotaId).stream()
                .filter(c -> CitaEstados.fueAtendida(c.getEstado()) && estaDisponible(c)).toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS guardarCita(CitaRq rq) throws BadRequestException {
        validarRq(rq);
        Cliente cliente = obtenerCliente(rq.getClienteId());
        Mascota mascota = obtenerMascota(rq.getMascotaId());
        Medico medico = obtenerMedico(rq.getMedicoId());
        usuarioActualExigirMedico(medico.getId());
        // Se admite cualquier hora del día de hoy (el médico puede registrar y atender hasta la última hora de hoy)
        if (rq.getFechaHora().toLocalDate().isBefore(java.time.LocalDate.now())) {
            throw new BadRequestException("No se pueden registrar citas en fechas anteriores a hoy.");
        }
        LocalDateTime fin = rq.getFechaHora().plusMinutes(duracion(rq.getDuracionMinutos(), DURACION_DEFECTO));
        validarReglas(cliente, mascota, medico, rq.getFechaHora(), fin, null);
        Cita cita = new Cita();
        cita.setCliente(cliente);
        cita.setMascota(mascota);
        cita.setMedico(medico);
        cita.setFechaHora(rq.getFechaHora());
        cita.setFechaHoraFin(fin);
        cita.setEstado(estadoInicial(rq.getEstado()));
        cita.setMotivo(rq.getMotivo());
        cita.setEspecializacionId(medico.getEspecializacionId().longValue());
        cita = citaRepository.save(cita);
        MiRespuestaRS respuesta = ok("Cita médica registrada exitosamente.");
        respuesta.setId(cita.getId());
        return respuesta;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS actualizarCita(CitaRq rq) throws BadRequestException {
        validarRq(rq);
        if (rq.getCitaId() == null || rq.getCitaId() <= 0) {
            throw new BadRequestException("El identificador de la cita a actualizar es obligatorio.");
        }
        Cita cita = citaRepository.bloquear(rq.getCitaId())
                .orElseThrow(() -> new NotFoundException("La cita a actualizar no existe."));
        usuarioActualExigirPropia(cita);
        exigirEditable(cita);
        Cliente cliente = obtenerCliente(rq.getClienteId());
        Mascota mascota = obtenerMascota(rq.getMascotaId());
        Medico medico = obtenerMedico(rq.getMedicoId());
        usuarioActualExigirMedico(medico.getId());
        LocalDateTime fin = rq.getFechaHora().plusMinutes(duracion(rq.getDuracionMinutos(), minutos(cita)));
        validarReglas(cliente, mascota, medico, rq.getFechaHora(), fin, cita.getId());
        cita.setCliente(cliente);
        cita.setMascota(mascota);
        cita.setMedico(medico);
        cita.setFechaHora(rq.getFechaHora());
        cita.setFechaHoraFin(fin);
        cita.setMotivo(rq.getMotivo());
        cita.setEspecializacionId(medico.getEspecializacionId().longValue());
        if (rq.getEstado() != null && !rq.getEstado().isBlank()) {
            String nuevo = rq.getEstado().trim().toUpperCase();
            if (!CitaEstados.TODOS.contains(nuevo)) {
                throw new BadRequestException("El estado '" + rq.getEstado() + "' no es válido. Estados permitidos: " + CitaEstados.TODOS);
            }
            if (!nuevo.equals(cita.getEstado()) && !List.of(CitaEstados.PENDIENTE, CitaEstados.CONFIRMADA).contains(nuevo)) {
                throw new BadRequestException("Use los servicios de cancelar o atender para cambiar a " + nuevo + ".");
            }
            cita.setEstado(nuevo);
        }
        citaRepository.save(cita);
        return ok("Cita médica actualizada exitosamente.");
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS cancelarCita(Long citaId, String motivo) throws BadRequestException {
        Cita cita = cargar(citaId);
        if (CitaEstados.CANCELADA.equals(cita.getEstado())) {
            throw new BadRequestException("La cita ya se encuentra en estado Cancelada.");
        }
        if (CitaEstados.fueAtendida(cita.getEstado())) {
            throw new BadRequestException("No es posible cancelar una cita que ya fue atendida.");
        }
        cita.setEstado(CitaEstados.CANCELADA);
        cita.setMotivoCancelacion(motivo == null || motivo.isBlank() ? "Sin motivo registrado" : motivo.trim());
        citaRepository.save(cita);
        return ok("Cita cancelada exitosamente.");
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS reprogramar(ReprogramarRq rq) {
        if (rq == null || rq.getFechaHora() == null) {
            throw new BadRequestException("La nueva fecha y hora es obligatoria");
        }
        Cita cita = cargar(rq.getCitaId());
        exigirEditable(cita);
        if (rq.getFechaHora().toLocalDate().isBefore(java.time.LocalDate.now())) {
            throw new BadRequestException("No se puede reprogramar a una fecha anterior a hoy.");
        }
        LocalDateTime fin = rq.getFechaHora().plusMinutes(duracion(rq.getDuracionMinutos(), minutos(cita)));
        validarReglas(cita.getCliente(), cita.getMascota(), cita.getMedico(), rq.getFechaHora(), fin, cita.getId());
        cita.setFechaHora(rq.getFechaHora());
        cita.setFechaHoraFin(fin);
        citaRepository.save(cita);
        return ok("Cita reprogramada exitosamente.");
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS confirmar(Long citaId) {
        Cita cita = cargar(citaId);
        if (!CitaEstados.PENDIENTE.equals(cita.getEstado())) {
            throw new BadRequestException("Solo se puede confirmar una cita PENDIENTE.");
        }
        cita.setEstado(CitaEstados.CONFIRMADA);
        citaRepository.save(cita);
        return ok("Cita confirmada.");
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS marcarAtendida(Long citaId) {
        Cita cita = cargar(citaId);
        if (!List.of(CitaEstados.PENDIENTE, CitaEstados.CONFIRMADA).contains(cita.getEstado())) {
            throw new BadRequestException("Solo se puede atender una cita PENDIENTE o CONFIRMADA.");
        }
        if (cita.getFechaHora().toLocalDate().isAfter(java.time.LocalDate.now())) {
            throw new BadRequestException("No se puede atender una cita programada para una fecha futura ("
                    + cita.getFechaHora().toLocalDate() + ").");
        }
        cita.setEstado(CitaEstados.ATENDIDA);
        citaRepository.save(cita);
        return ok("Cita marcada como atendida.");
    }

    /** {@inheritDoc} */
    @Override
    public List<CitaCalendarioRs> calendario(Long medicoId, LocalDateTime desde, LocalDateTime hasta) {
        return citasEnRango(medicoId, desde, hasta).stream()
                .filter(this::estaDisponible)
                .map(c -> new CitaCalendarioRs(
                    c.getId(),
                    c.getMascota().getNombreMascota() + " - " + c.getCliente().getNombres() + " " + c.getCliente().getApellidos(),
                    c.getFechaHora(), c.getFechaHoraFin(), c.getEstado(),
                    c.getMascota().getNombreMascota(),
                    c.getCliente().getNombres() + " " + c.getCliente().getApellidos(),
                    c.getMotivo(), c.getMedico().getId(),
                    c.getMedico().getNombres() + " " + c.getMedico().getApellidos())).toList();
    }

    /** {@inheritDoc} */
    @Override
    public List<Cita> historialMascota(Integer mascotaId) {
        obtenerMascota(mascotaId);
        Long propio = medicoRestringido();
        return citaRepository.findAllByMascota_MascotaIdOrderByFechaHoraDesc(mascotaId).stream()
                .filter(c -> propio == null || c.getMedico().getId().equals(propio))
                .filter(this::estaDisponible)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Long> resumenPorEstado(Long medicoId, LocalDateTime desde, LocalDateTime hasta) {
        Map<String, Long> r = new LinkedHashMap<>();
        CitaEstados.TODOS.forEach(e -> r.put(e, 0L));
        for (Cita c : citasEnRango(medicoId, desde, hasta)) {
            r.merge(c.getEstado(), 1L, Long::sum);
        }
        r.put("TOTAL", r.values().stream().mapToLong(Long::longValue).sum());
        return r;
    }

    /** {@inheritDoc} */
    @Override
    public byte[] reporteExcel(Long medicoId, LocalDateTime desde, LocalDateTime hasta) {
        List<List<Object>> filas = new ArrayList<>();
        for (Cita c : citasEnRango(medicoId, desde, hasta)) {
            filas.add(List.of(c.getId(), c.getFechaHora().toString().replace('T', ' '),
                    c.getFechaHoraFin() == null ? "" : c.getFechaHoraFin().toString().replace('T', ' '),
                    c.getEstado(), c.getMascota().getNombreMascota(),
                    c.getCliente().getNombres() + " " + c.getCliente().getApellidos(),
                    c.getMedico().getNombres() + " " + c.getMedico().getApellidos(),
                    c.getMotivo() == null ? "" : c.getMotivo()));
        }
        Map<String, Object> totales = new LinkedHashMap<>(resumenPorEstado(medicoId, desde, hasta));
        return ExcelUtil.generar("Citas", "Reporte de citas " + desde.toLocalDate() + " a " + hasta.toLocalDate(),
                List.of("Id", "Inicio", "Fin", "Estado", "Mascota", "Propietario", "Médico", "Motivo"), filas, totales);
    }

    // ------------------------------------------------------------------ utilidades

    /** Citas del rango respetando que un MEDICO solo ve las suyas. Filtera usuarios inactivos. */
    private List<Cita> citasEnRango(Long medicoId, LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null || hasta == null) {
            throw new BadRequestException("Los parámetros desde y hasta son obligatorios");
        }
        if (!hasta.isAfter(desde)) {
            throw new BadRequestException("El rango de fechas es inválido: 'hasta' debe ser posterior a 'desde'");
        }
        Long propio = medicoRestringido();
        Long filtro = propio != null ? propio : medicoId;
        if (propio != null && medicoId != null && !medicoId.equals(propio)) {
            throw new AccessDeniedException("Un médico solo consulta sus propias citas");
        }
        return citaRepository.enRango(filtro, desde, hasta).stream().filter(this::estaDisponible).toList();
    }

    /**
     * True si la cita debe aparecer en registros: el cliente y el médico asociados
     * están activos en el sistema.
     */
    private boolean estaDisponible(Cita c) {
        if (c.getCliente() == null || c.getMedico() == null) {
            return false;
        }
        return Boolean.TRUE.equals(c.getCliente().getActivo()) && Boolean.TRUE.equals(c.getMedico().getActivo());
    }

    /** @return id del médico si el usuario actual es MEDICO; {@code null} si no aplica restricción. */
    private Long medicoRestringido() {
        try {
            return usuarioActual.tieneRol(Roles.MEDICO) ? usuarioActual.medicoId() : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private void usuarioActualExigirPropia(Cita c) {
        usuarioActualExigirMedico(c.getMedico().getId());
    }

    private void usuarioActualExigirMedico(Long medicoId) {
        try {
            usuarioActual.exigirPropioSiEsMedico(medicoId);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            // sin sesión (uso interno): sin restricción adicional
        }
    }

    private Cita cargar(Long citaId) {
        if (citaId == null || citaId <= 0) {
            throw new BadRequestException("El identificador de la cita es obligatorio.");
        }
        Cita c = citaRepository.bloquear(citaId)
                .orElseThrow(() -> new NotFoundException("La cita con id " + citaId + " no existe."));
        usuarioActualExigirPropia(c);
        return c;
    }

    private void exigirEditable(Cita cita) {
        if (!List.of(CitaEstados.PENDIENTE, CitaEstados.CONFIRMADA).contains(cita.getEstado())) {
            throw new BadRequestException("Solo se pueden editar o reprogramar citas PENDIENTES o CONFIRMADAS (estado actual: "
                    + cita.getEstado() + ").");
        }
    }

    private int minutos(Cita c) {
        return c.getFechaHoraFin() == null ? DURACION_DEFECTO
                : (int) java.time.Duration.between(c.getFechaHora(), c.getFechaHoraFin()).toMinutes();
    }

    private int duracion(Integer pedida, int defecto) {
        int d = pedida == null ? defecto : pedida;
        if (d < 10 || d > 240) {
            throw new BadRequestException("La duración debe estar entre 10 y 240 minutos.");
        }
        return d;
    }

    private String estadoInicial(String estado) {
        if (estado == null || estado.isBlank()) {
            return CitaEstados.PENDIENTE;
        }
        String e = estado.trim().toUpperCase();
        if (e.equals("PROGRAMADA")) {
            return CitaEstados.PENDIENTE;
        }
        if (!e.equals(CitaEstados.PENDIENTE) && !e.equals(CitaEstados.CONFIRMADA)) {
            throw new BadRequestException("Una cita nueva solo puede iniciar PENDIENTE o CONFIRMADA.");
        }
        return e;
    }

    private void validarRq(CitaRq rq) {
        if (rq == null) {
            throw new BadRequestException("El objeto de solicitud de cita no puede ser nulo.");
        }
        if (rq.getClienteId() == null || rq.getClienteId() <= 0) {
            throw new BadRequestException("El id del cliente es obligatorio y debe ser mayor a cero.");
        }
        if (rq.getMascotaId() == null || rq.getMascotaId() <= 0) {
            throw new BadRequestException("El id de la mascota es obligatorio y debe ser mayor a cero.");
        }
        if (rq.getMedicoId() == null || rq.getMedicoId() <= 0) {
            throw new BadRequestException("El id del médico veterinario es obligatorio y debe ser mayor a cero.");
        }
        if (rq.getFechaHora() == null) {
            throw new BadRequestException("La fecha y hora de la cita es obligatoria.");
        }
        if (rq.getMotivo() == null || rq.getMotivo().isBlank()) {
            throw new BadRequestException("El motivo de la cita es obligatorio.");
        }
    }

    /** Aplica RN1 a RN3 y RN6 (sin restricción de horario del veterinario). */
    private void validarReglas(Cliente cliente, Mascota mascota, Medico medico,
                               LocalDateTime inicio, LocalDateTime fin, Long excluir) {
        if (mascota.getCliente() == null || !mascota.getCliente().getClienteId().equals(cliente.getClienteId())) {
            throw new BadRequestException("La mascota '" + mascota.getNombreMascota() + "' no pertenece al cliente seleccionado ("
                    + cliente.getNombres() + " " + cliente.getApellidos() + ").");
        }
        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            throw new BadRequestException("El cliente está inactivo y no puede crear ni programar citas.");
        }
        if (!Boolean.TRUE.equals(medico.getActivo())) {
            throw new BadRequestException("El médico veterinario está inactivo y no puede recibir citas.");
        }
        long ex = excluir == null ? -1L : excluir;
        if (!citaRepository.solapadasMedico(medico.getId(), inicio, fin, ex).isEmpty()) {
            throw new ConflictException("El médico veterinario ya tiene una cita que se solapa con ese horario.");
        }
        if (!citaRepository.solapadasMascota(mascota.getMascotaId(), inicio, fin, ex).isEmpty()) {
            throw new ConflictException("La mascota ya tiene otra cita activa en un horario que se solapa.");
        }
    }

    private Cliente obtenerCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("El cliente con id " + id + " no existe."));
    }

    private Mascota obtenerMascota(Integer id) {
        return mascotaRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("La mascota con id " + id + " no existe."));
    }

    private Medico obtenerMedico(Long id) {
        return medicoRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("El médico veterinario con id " + id + " no existe."));
    }

    private MiRespuestaRS ok(String mensaje) {
        MiRespuestaRS r = new MiRespuestaRS();
        r.setStatus(200);
        r.setMessage(mensaje);
        return r;
    }
}
