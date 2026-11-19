package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Tarifa;
import com.uniminuto.clinica.entity.TarifaLog;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.TarifaRq;
import com.uniminuto.clinica.repository.EspecializacionRepository;
import com.uniminuto.clinica.repository.TarifaLogRepository;
import com.uniminuto.clinica.repository.TarifaRepository;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.service.TarifaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación de tarifas: cada alta o cambio queda en {@code tarifa_log}.
 */
@Service
public class TarifaServiceImpl implements TarifaService {

    @Autowired
    private TarifaRepository tarifaRepository;

    @Autowired
    private TarifaLogRepository logRepository;

    @Autowired
    private EspecializacionRepository especializacionRepository;

    @Autowired
    private UsuarioActual usuarioActual;

    /** {@inheritDoc} */
    @Override
    public List<Tarifa> listar(Long especializacionId, boolean todas) {
        List<Tarifa> base = todas ? tarifaRepository.findAll() : tarifaRepository.findAllByActivaTrueOrderByEspecializacionIdAscServicioAsc();
        return base.stream().filter(t -> especializacionId == null || t.getEspecializacionId().equals(especializacionId)).toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Tarifa crear(TarifaRq rq) {
        validar(rq);
        Tarifa t = new Tarifa();
        aplicar(t, rq);
        t = tarifaRepository.save(t);
        log(t, "CREACION", null, null);
        return t;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Tarifa actualizar(TarifaRq rq) {
        if (rq == null || rq.getId() == null) {
            throw new BadRequestException("El id de la tarifa es obligatorio");
        }
        validar(rq);
        Tarifa t = buscar(rq.getId());
        BigDecimal valorAnt = t.getValor();
        BigDecimal ivaAnt = t.getIvaPorcentaje();
        aplicar(t, rq);
        t = tarifaRepository.save(t);
        log(t, "MODIFICACION", valorAnt, ivaAnt);
        return t;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Tarifa cambiarEstado(Long id, boolean activa) {
        Tarifa t = buscar(id);
        t.setActiva(activa);
        t = tarifaRepository.save(t);
        log(t, activa ? "ACTIVACION" : "INACTIVACION", t.getValor(), t.getIvaPorcentaje());
        return t;
    }

    /** {@inheritDoc} */
    @Override
    public List<TarifaLog> historial(Long id) {
        buscar(id);
        return logRepository.findAllByTarifaIdOrderByFechaDesc(id);
    }

    private Tarifa buscar(Long id) {
        return tarifaRepository.findById(id).orElseThrow(() -> new NotFoundException("La tarifa " + id + " no existe"));
    }

    private void validar(TarifaRq rq) {
        if (rq == null || rq.getEspecializacionId() == null || rq.getServicio() == null || rq.getServicio().isBlank()) {
            throw new BadRequestException("La especialidad y el servicio son obligatorios");
        }
        if (!especializacionRepository.existsById(rq.getEspecializacionId())) {
            throw new BadRequestException("La especialidad " + rq.getEspecializacionId() + " no existe");
        }
        if (rq.getValor() == null || rq.getValor().signum() < 0) {
            throw new BadRequestException("El valor es obligatorio y no puede ser negativo");
        }
        if (rq.getIvaPorcentaje() != null && (rq.getIvaPorcentaje().signum() < 0
                || rq.getIvaPorcentaje().compareTo(new BigDecimal("100")) > 0)) {
            throw new BadRequestException("El IVA debe estar entre 0 y 100");
        }
    }

    private void aplicar(Tarifa t, TarifaRq rq) {
        t.setEspecializacionId(rq.getEspecializacionId());
        t.setServicio(rq.getServicio().trim());
        t.setValor(rq.getValor());
        t.setIvaPorcentaje(rq.getIvaPorcentaje() == null ? BigDecimal.ZERO : rq.getIvaPorcentaje());
        t.setActiva(rq.getActiva() == null ? Boolean.TRUE : rq.getActiva());
    }

    private void log(Tarifa t, String accion, BigDecimal valorAnt, BigDecimal ivaAnt) {
        TarifaLog l = new TarifaLog();
        l.setTarifaId(t.getId());
        l.setAccion(accion);
        l.setValorAnterior(valorAnt);
        l.setValorNuevo(t.getValor());
        l.setIvaAnterior(ivaAnt);
        l.setIvaNuevo(t.getIvaPorcentaje());
        l.setUsuarioId(usuarioActual.id());
        l.setFecha(LocalDateTime.now());
        logRepository.save(l);
    }
}
