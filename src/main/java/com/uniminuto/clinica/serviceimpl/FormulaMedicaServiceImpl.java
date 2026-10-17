package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación de {@link FormulaMedicaService} usando Spring Data JPA.
 */
@Service
public class FormulaMedicaServiceImpl implements FormulaMedicaService {

    /**
     * Duración máxima permitida para un tratamiento, en días.
     */
    private static final int DURACION_MAXIMA_DIAS = 365;

    /**
     * Longitud máxima permitida para la frecuencia de administración.
     */
    private static final int LONGITUD_MAXIMA_FRECUENCIA = 100;

    /**
     * Estado que debe tener una cita para poder emitir una fórmula sobre ella.
     */
    private static final String ESTADO_CITA_ATENDIDA = "atendida";

    /**
     * Repositorio JPA para el acceso a datos de fórmulas médicas.
     */
    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;

    /**
     * Repositorio JPA para el acceso a datos de citas, usado para validar la
     * consulta en la que se emite la fórmula.
     */
    @Autowired
    private CitaRepository citaRepository;

    /**
     * Repositorio JPA para el acceso a datos de medicamentos, usado para validar
     * el medicamento prescrito.
     */
    @Autowired
    private MedicamentoRepository medicamentoRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FormulaMedica> listarFormulasMedicas() throws BadRequestException {
        return this.formulaMedicaRepository.findAllByOrderByFechaCreacionRegistroDesc();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FormulaMedica consultarFormulaMedica(Long formulaId) throws BadRequestException {
        if (formulaId == null || formulaId <= 0) {
            throw new BadRequestException("El id de la formula medica no puede ser nulo o negativo");
        }
        return this.formulaMedicaRepository.findById(formulaId)
                .orElseThrow(() -> new BadRequestException("La formula medica seleccionada no es valida"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Cita> listarCitasDisponibles(Integer mascotaId) throws BadRequestException {
        if (mascotaId == null || mascotaId <= 0) {
            throw new BadRequestException("El id de la mascota no puede ser nulo o negativo");
        }
        return this.citaRepository
                .findAllByMascota_MascotaIdAndEstadoIgnoreCaseOrderByFechaHoraDesc(mascotaId, ESTADO_CITA_ATENDIDA);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS guardarFormulaMedica(FormulaMedicaRq formulaMedicaRq) throws BadRequestException {
        this.validarFormulaMedicaRq(formulaMedicaRq);

        FormulaMedica formulaMedica = new FormulaMedica();
        this.asignarDatosValidados(formulaMedica, formulaMedicaRq);
        formulaMedica.setFechaCreacionRegistro(LocalDateTime.now());

        this.formulaMedicaRepository.save(formulaMedica);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Formula medica guardada exitosamente");
        return respuesta;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS actualizarFormulaMedica(FormulaMedicaRq formulaMedicaRq) throws BadRequestException {
        this.validarFormulaMedicaRq(formulaMedicaRq);

        FormulaMedica formulaExistente = this.consultarFormulaMedica(formulaMedicaRq.getFormulaId());
        this.asignarDatosValidados(formulaExistente, formulaMedicaRq);
        formulaExistente.setFechaActualizacionRegistro(LocalDateTime.now());

        this.formulaMedicaRepository.save(formulaExistente);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Formula medica actualizada exitosamente");
        return respuesta;
    }

    /**
     * Valida las referencias (cita y medicamento) y la fecha de emisión de la
     * solicitud, y copia los datos a la fórmula médica indicada.
     *
     * @param formulaMedica   fórmula médica (nueva o existente) que recibe los datos.
     * @param formulaMedicaRq datos de entrada ya validados en su formato.
     * @throws BadRequestException si alguna referencia o la fecha de emisión no es válida.
     */
    private void asignarDatosValidados(FormulaMedica formulaMedica, FormulaMedicaRq formulaMedicaRq)
            throws BadRequestException {
        Cita cita = this.obtenerCitaValida(formulaMedicaRq.getCitaId(), formulaMedicaRq.getMascotaId());

        LocalDateTime fechaEmision = formulaMedicaRq.getFechaEmision() != null
                ? formulaMedicaRq.getFechaEmision()
                : LocalDateTime.now();
        if (fechaEmision.isBefore(cita.getFechaHora())) {
            throw new BadRequestException("La fecha de emision no puede ser anterior a la fecha de la consulta");
        }

        Medicamento medicamento = this.obtenerMedicamentoVigente(
                formulaMedicaRq.getMedicamentoId(), fechaEmision);

        formulaMedica.setCita(cita);
        formulaMedica.setMedicamento(medicamento);
        formulaMedica.setDosis(formulaMedicaRq.getDosis().trim());
        formulaMedica.setFrecuencia(formulaMedicaRq.getFrecuencia().trim());
        formulaMedica.setDuracionDias(formulaMedicaRq.getDuracionDias());
        formulaMedica.setIndicaciones(formulaMedicaRq.getIndicaciones() != null
                ? formulaMedicaRq.getIndicaciones().trim()
                : null);
        formulaMedica.setFechaEmision(fechaEmision);
    }

    /**
     * Valida que los datos de entrada de una fórmula médica sean correctos.
     * El {@code formulaId} solo se valida cuando se envía (obligatorio al actualizar,
     * lo verifica {@link #consultarFormulaMedica(Long)}).
     *
     * @param formulaMedicaRq datos a validar.
     * @throws BadRequestException si algún dato obligatorio es nulo o inválido.
     */
    private void validarFormulaMedicaRq(FormulaMedicaRq formulaMedicaRq) throws BadRequestException {
        if (formulaMedicaRq == null) {
            throw new BadRequestException("El objeto FormulaMedicaRq no puede ser nulo");
        }
        if (formulaMedicaRq.getMascotaId() == null || formulaMedicaRq.getMascotaId() <= 0) {
            throw new BadRequestException("El id de la mascota no puede ser nulo o negativo");
        }
        if (formulaMedicaRq.getCitaId() == null || formulaMedicaRq.getCitaId() <= 0) {
            throw new BadRequestException("El id de la cita no puede ser nulo o negativo");
        }
        if (formulaMedicaRq.getMedicamentoId() == null || formulaMedicaRq.getMedicamentoId() <= 0) {
            throw new BadRequestException("El id del medicamento no puede ser nulo o negativo");
        }
        if (formulaMedicaRq.getDosis() == null || formulaMedicaRq.getDosis().isBlank()) {
            throw new BadRequestException("La dosis es obligatoria");
        }
        if (formulaMedicaRq.getFrecuencia() == null || formulaMedicaRq.getFrecuencia().isBlank()) {
            throw new BadRequestException("La frecuencia de administracion es obligatoria");
        }
        if (formulaMedicaRq.getFrecuencia().trim().length() > LONGITUD_MAXIMA_FRECUENCIA) {
            throw new BadRequestException("La frecuencia no puede superar los "
                    + LONGITUD_MAXIMA_FRECUENCIA + " caracteres");
        }
        if (formulaMedicaRq.getDuracionDias() == null
                || formulaMedicaRq.getDuracionDias() < 1
                || formulaMedicaRq.getDuracionDias() > DURACION_MAXIMA_DIAS) {
            throw new BadRequestException("La duracion del tratamiento debe estar entre 1 y "
                    + DURACION_MAXIMA_DIAS + " dias");
        }
    }

    /**
     * Obtiene la cita indicada validando que exista, que corresponda a la mascota
     * indicada y que se encuentre en estado {@code atendida} (la fórmula solo se
     * emite sobre una consulta ya realizada).
     *
     * @param citaId    identificador de la cita.
     * @param mascotaId identificador de la mascota a la que se prescribe.
     * @return la cita encontrada.
     * @throws BadRequestException si la cita no existe, es de otra mascota o no está atendida.
     */
    private Cita obtenerCitaValida(Long citaId, Integer mascotaId) throws BadRequestException {
        Cita cita = this.citaRepository.findById(citaId)
                .orElseThrow(() -> new BadRequestException("La cita seleccionada no es valida"));
        if (cita.getMascota() == null
                || cita.getMascota().getMascotaId() == null
                || !cita.getMascota().getMascotaId().equals(mascotaId)) {
            throw new BadRequestException("La cita seleccionada no corresponde a la mascota indicada");
        }
        if (cita.getEstado() == null || !ESTADO_CITA_ATENDIDA.equalsIgnoreCase(cita.getEstado().trim())) {
            throw new BadRequestException("Solo se pueden emitir formulas sobre citas en estado atendida");
        }
        return cita;
    }

    /**
     * Obtiene el medicamento indicado validando que exista y que no esté vencido
     * en la fecha de emisión de la fórmula.
     *
     * @param medicamentoId identificador del medicamento.
     * @param fechaEmision  fecha de emisión de la fórmula.
     * @return el medicamento encontrado.
     * @throws BadRequestException si el medicamento no existe o está vencido.
     */
    private Medicamento obtenerMedicamentoVigente(Long medicamentoId, LocalDateTime fechaEmision)
            throws BadRequestException {
        Medicamento medicamento = this.medicamentoRepository.findById(medicamentoId)
                .orElseThrow(() -> new BadRequestException("El medicamento seleccionado no es valido"));
        if (medicamento.getFechaVence() != null
                && medicamento.getFechaVence().isBefore(fechaEmision.toLocalDate())) {
            throw new BadRequestException("El medicamento seleccionado se encuentra vencido");
        }
        return medicamento;
    }
}
