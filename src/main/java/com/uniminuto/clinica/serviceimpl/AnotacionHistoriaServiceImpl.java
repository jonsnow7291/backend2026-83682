package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de {@link AnotacionHistoriaService} usando Spring Data JPA.
 */
@Service
public class AnotacionHistoriaServiceImpl implements AnotacionHistoriaService {

    /**
     * Repositorio JPA para el acceso a datos de anotaciones de historia médica.
     */
    @Autowired
    private AnotacionHistoriaRepository anotacionHistoriaRepository;

    /**
     * Repositorio JPA para el acceso a datos de historias médicas, usado para validar referencias.
     */
    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;

    /**
     * Repositorio JPA para el acceso a datos de médicos, usado para validar referencias.
     */
    @Autowired
    private MedicoRepository medicoRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AnotacionHistoria> listarAnotaciones(Long historiaId) throws BadRequestException {
        HistoriaMedica historiaMedica = this.obtenerHistoriaMedicaValida(historiaId);
        return this.anotacionHistoriaRepository.findAllByHistoriaMedicaOrderByFechaDesc(historiaMedica);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS guardarAnotacionHistoria(AnotacionHistoriaRq anotacionHistoriaRq)
            throws BadRequestException {
        this.validarAnotacionHistoriaRq(anotacionHistoriaRq);

        HistoriaMedica historiaMedica = this.obtenerHistoriaMedicaValida(anotacionHistoriaRq.getHistoriaId());
        Medico medico = this.obtenerMedicoValido(anotacionHistoriaRq.getMedicoId());

        AnotacionHistoria anotacion = new AnotacionHistoria();
        anotacion.setHistoriaMedica(historiaMedica);
        anotacion.setMedico(medico);
        anotacion.setDescripcion(anotacionHistoriaRq.getDescripcion());
        anotacion.setFecha(LocalDateTime.now());

        this.anotacionHistoriaRepository.save(anotacion);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Anotacion de historia guardada exitosamente");
        return respuesta;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS actualizarAnotacionHistoria(AnotacionHistoriaRq anotacionHistoriaRq)
            throws BadRequestException {
        this.validarAnotacionHistoriaRq(anotacionHistoriaRq);

        if (anotacionHistoriaRq.getAnotacionId() == null || anotacionHistoriaRq.getAnotacionId() <= 0) {
            throw new BadRequestException("El id de la anotacion no puede ser nulo o negativo");
        }

        Optional<AnotacionHistoria> optAnotacion =
                this.anotacionHistoriaRepository.findById(anotacionHistoriaRq.getAnotacionId());
        if (optAnotacion.isEmpty()) {
            throw new BadRequestException("La anotacion seleccionada no es valida");
        }

        HistoriaMedica historiaMedica = this.obtenerHistoriaMedicaValida(anotacionHistoriaRq.getHistoriaId());
        Medico medico = this.obtenerMedicoValido(anotacionHistoriaRq.getMedicoId());

        AnotacionHistoria anotacionExistente = optAnotacion.get();
        anotacionExistente.setHistoriaMedica(historiaMedica);
        anotacionExistente.setMedico(medico);
        anotacionExistente.setDescripcion(anotacionHistoriaRq.getDescripcion());

        this.anotacionHistoriaRepository.save(anotacionExistente);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Anotacion de historia actualizada exitosamente");
        return respuesta;
    }

    /**
     * Valida que los datos comunes de creación/actualización de una anotación sean correctos.
     *
     * @param anotacionHistoriaRq datos a validar.
     * @throws BadRequestException si algún dato es nulo, vacío o inválido.
     */
    private void validarAnotacionHistoriaRq(AnotacionHistoriaRq anotacionHistoriaRq) throws BadRequestException {
        if (anotacionHistoriaRq == null) {
            throw new BadRequestException("El objeto AnotacionHistoriaRq no puede ser nulo");
        }
        if (anotacionHistoriaRq.getHistoriaId() == null || anotacionHistoriaRq.getHistoriaId() <= 0) {
            throw new BadRequestException("El id de la historia medica no puede ser nulo o negativo");
        }
        if (anotacionHistoriaRq.getMedicoId() == null || anotacionHistoriaRq.getMedicoId() <= 0) {
            throw new BadRequestException("El id del medico no puede ser nulo o negativo");
        }
        if (anotacionHistoriaRq.getDescripcion() == null || anotacionHistoriaRq.getDescripcion().isEmpty()) {
            throw new BadRequestException("La descripcion de la anotacion no puede ser nula o vacía");
        }
    }

    /**
     * Obtiene la historia médica indicada o lanza una excepción si no existe.
     *
     * @param historiaId identificador de la historia médica.
     * @return la historia médica encontrada.
     * @throws BadRequestException si el identificador es inválido o la historia no existe.
     */
    private HistoriaMedica obtenerHistoriaMedicaValida(Long historiaId) throws BadRequestException {
        if (historiaId == null || historiaId <= 0) {
            throw new BadRequestException("El id de la historia medica no puede ser nulo o negativo");
        }
        return this.historiaMedicaRepository.findById(historiaId)
                .orElseThrow(() -> new BadRequestException("La historia medica seleccionada no es valida"));
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
