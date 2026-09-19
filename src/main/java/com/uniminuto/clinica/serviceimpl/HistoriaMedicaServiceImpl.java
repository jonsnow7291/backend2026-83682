package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
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

        HistoriaMedica historiaMedica = new HistoriaMedica();
        historiaMedica.setMascota(mascota);
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

        HistoriaMedica historiaExistente = optHistoriaMedica.get();
        historiaExistente.setMascota(mascota);

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

        this.historiaMedicaRepository.delete(optHistoriaMedica.get());

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Historia medica eliminada exitosamente");
        return respuesta;
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
