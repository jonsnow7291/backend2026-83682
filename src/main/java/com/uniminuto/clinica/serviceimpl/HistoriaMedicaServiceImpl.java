package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.service.HistoriaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de {@link HistoriaMedicaService} usando Spring Data JPA.
 */
@Service
public class HistoriaMedicaServiceImpl implements HistoriaMedicaService {

    /**
     * Repositorio JPA para el acceso a datos de historias médicas.
     */
    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;

    /**
     * Repositorio JPA para el acceso a datos de mascotas, usado para validar referencias.
     */
    @Autowired
    private MascotaRepository mascotaRepository;

    /**
     * Repositorio JPA para el acceso a datos de clientes, usado para validar que la
     * mascota pertenezca al cliente indicado.
     */
    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Repositorio JPA para el acceso a datos de citas, usado para validar que la
     * cita asociada esté en estado {@code atendida}.
     */
    @Autowired
    private CitaRepository citaRepository;

    /**
     * Repositorio JPA para el acceso a datos de anotaciones, usado para impedir la
     * eliminación de historias con información clínica asociada.
     */
    @Autowired
    private AnotacionHistoriaRepository anotacionHistoriaRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HistoriaMedica> listarHistoriasMedicas(LocalDateTime fechaInicial, LocalDateTime fechaFinal)
            throws BadRequestException {
        if (fechaInicial == null && fechaFinal == null) {
            return this.historiaMedicaRepository.findAllByOrderByFechaCreacionDesc();
        }
        if (fechaInicial == null || fechaFinal == null) {
            throw new BadRequestException("Debe proporcionar tanto la fecha inicial como la fecha final, o ninguna");
        }
        if (fechaInicial.isAfter(fechaFinal)) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la fecha final");
        }
        return this.historiaMedicaRepository
                .findAllByFechaCreacionBetweenOrderByFechaCreacionDesc(fechaInicial, fechaFinal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS guardarHistoriaMedica(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        this.validarHistoriaMedicaRq(historiaMedicaRq);

        Mascota mascota = this.obtenerMascotaValida(historiaMedicaRq.getMascotaId());
        this.validarClienteAsociado(mascota, historiaMedicaRq.getClienteId());
        Cita cita = this.validarCitaAsociada(mascota, historiaMedicaRq.getCitaId());
        this.validarCitaSinHistoria(cita.getId(), null);

        HistoriaMedica historiaMedica = new HistoriaMedica();
        this.asignarDatos(historiaMedica, mascota, cita, historiaMedicaRq);
        historiaMedica.setFechaCreacion(LocalDateTime.now());

        this.historiaMedicaRepository.save(historiaMedica);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Historia medica guardada exitosamente");
        return respuesta;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS actualizarHistoriaMedica(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        this.validarHistoriaMedicaRq(historiaMedicaRq);

        if (historiaMedicaRq.getHistoriaId() == null || historiaMedicaRq.getHistoriaId() <= 0) {
            throw new BadRequestException("El id de la historia medica no puede ser nulo o negativo");
        }

        Optional<HistoriaMedica> optHistoriaMedica =
                this.historiaMedicaRepository.findById(historiaMedicaRq.getHistoriaId());
        if (optHistoriaMedica.isEmpty()) {
            throw new BadRequestException("La historia medica seleccionada no es valida");
        }

        Mascota mascota = this.obtenerMascotaValida(historiaMedicaRq.getMascotaId());
        this.validarClienteAsociado(mascota, historiaMedicaRq.getClienteId());
        Cita cita = this.validarCitaAsociada(mascota, historiaMedicaRq.getCitaId());
        this.validarCitaSinHistoria(cita.getId(), historiaMedicaRq.getHistoriaId());

        HistoriaMedica historiaExistente = optHistoriaMedica.get();
        this.asignarDatos(historiaExistente, mascota, cita, historiaMedicaRq);

        this.historiaMedicaRepository.save(historiaExistente);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Historia medica actualizada exitosamente");
        return respuesta;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS eliminarHistoriaMedica(Long historiaId) throws BadRequestException {
        if (historiaId == null || historiaId <= 0) {
            throw new BadRequestException("El id de la historia medica no puede ser nulo o negativo");
        }

        Optional<HistoriaMedica> optHistoriaMedica = this.historiaMedicaRepository.findById(historiaId);
        if (optHistoriaMedica.isEmpty()) {
            throw new BadRequestException("La historia medica seleccionada no es valida");
        }

        if (this.anotacionHistoriaRepository.existsByHistoriaMedica_Id(historiaId)) {
            throw new BadRequestException(
                    "La historia medica tiene anotaciones clinicas registradas y no puede eliminarse");
        }

        this.historiaMedicaRepository.delete(optHistoriaMedica.get());

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Historia medica eliminada exitosamente");
        return respuesta;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HistoriaMedica consultarHistoriaMedica(Long historiaId) throws BadRequestException {
        if (historiaId == null || historiaId <= 0) {
            throw new BadRequestException("El id de la historia medica no puede ser nulo o negativo");
        }
        return this.historiaMedicaRepository.findById(historiaId)
                .orElseThrow(() -> new BadRequestException("La historia medica seleccionada no es valida"));
    }

    /**
     * Valida que los datos de entrada de una historia médica sean correctos.
     *
     * @param historiaMedicaRq datos a validar.
     * @throws BadRequestException si algún dato es nulo o inválido.
     */
    private void validarHistoriaMedicaRq(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        if (historiaMedicaRq == null) {
            throw new BadRequestException("El objeto HistoriaMedicaRq no puede ser nulo");
        }
        if (historiaMedicaRq.getMascotaId() == null || historiaMedicaRq.getMascotaId() <= 0) {
            throw new BadRequestException("El id de la mascota no puede ser nulo o negativo");
        }
        if (historiaMedicaRq.getClienteId() == null || historiaMedicaRq.getClienteId() <= 0) {
            throw new BadRequestException("El id del cliente no puede ser nulo o negativo");
        }
        if (historiaMedicaRq.getCitaId() == null || historiaMedicaRq.getCitaId() <= 0) {
            throw new BadRequestException("El id de la cita no puede ser nulo o negativo");
        }
        if (historiaMedicaRq.getDiagnostico() == null || historiaMedicaRq.getDiagnostico().isBlank()) {
            throw new BadRequestException("El diagnostico de la historia medica es obligatorio");
        }
    }

    /**
     * Copia a la historia médica la mascota, la cita y los datos clínicos de la solicitud.
     *
     * @param historiaMedica   historia médica (nueva o existente) que recibe los datos.
     * @param mascota          mascota validada.
     * @param cita             cita atendida validada.
     * @param historiaMedicaRq datos de entrada validados.
     */
    private void asignarDatos(HistoriaMedica historiaMedica, Mascota mascota, Cita cita,
                              HistoriaMedicaRq historiaMedicaRq) {
        historiaMedica.setMascota(mascota);
        historiaMedica.setCita(cita);
        historiaMedica.setDiagnostico(historiaMedicaRq.getDiagnostico().trim());
        historiaMedica.setObservaciones(historiaMedicaRq.getObservaciones() != null
                ? historiaMedicaRq.getObservaciones().trim()
                : null);
    }

    /**
     * Valida que la cita no tenga ya otra historia clínica registrada, para evitar
     * duplicidad de información.
     *
     * @param citaId              identificador de la cita.
     * @param historiaIdExcluir   historia que se está actualizando ({@code null} al crear).
     * @throws BadRequestException si la cita ya tiene otra historia clínica.
     */
    private void validarCitaSinHistoria(Long citaId, Long historiaIdExcluir) throws BadRequestException {
        boolean existeOtra = this.historiaMedicaRepository.findAllByCita_Id(citaId).stream()
                .anyMatch(historia -> !historia.getId().equals(historiaIdExcluir));
        if (existeOtra) {
            throw new BadRequestException("La cita seleccionada ya tiene una historia clinica registrada");
        }
    }

    /**
     * Valida que la mascota indicada pertenezca al cliente indicado.
     *
     * @param mascota   mascota seleccionada.
     * @param clienteId identificador del cliente.
     * @throws BadRequestException si el cliente no existe o la mascota no le pertenece.
     */
    private void validarClienteAsociado(Mascota mascota, Long clienteId) throws BadRequestException {
        Cliente cliente = this.clienteRepository.findById(clienteId)
                .orElseThrow(() -> new BadRequestException("El cliente seleccionado no es valido"));
        if (mascota.getCliente() == null
                || mascota.getCliente().getClienteId() == null
                || !mascota.getCliente().getClienteId().equals(cliente.getClienteId())) {
            throw new BadRequestException("La mascota seleccionada no pertenece al cliente indicado");
        }
    }

    /**
     * Valida que la cita indicada exista, corresponda a la mascota y se encuentre
     * en estado {@code atendida}.
     *
     * @param mascota mascota seleccionada.
     * @param citaId  identificador de la cita.
     * @return la cita validada.
     * @throws BadRequestException si la cita no es válida o no está atendida.
     */
    private Cita validarCitaAsociada(Mascota mascota, Long citaId) throws BadRequestException {
        Cita cita = this.citaRepository.findById(citaId)
                .orElseThrow(() -> new BadRequestException("La cita seleccionada no es valida"));
        if (cita.getMascota() == null
                || cita.getMascota().getMascotaId() == null
                || !cita.getMascota().getMascotaId().equals(mascota.getMascotaId())) {
            throw new BadRequestException("La cita seleccionada no corresponde a la mascota indicada");
        }
        if (cita.getEstado() == null || !"atendida".equalsIgnoreCase(cita.getEstado().trim())) {
            throw new BadRequestException("La cita asociada debe estar en estado atendida");
        }
        return cita;
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
}