package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.HorarioVeterinario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.HorarioRq;
import com.uniminuto.clinica.repository.HorarioRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.HorarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD de horarios de veterinarios con validación de franjas y de cruces.
 */
@Service
public class HorarioServiceImpl implements HorarioService {

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    /** {@inheritDoc} */
    @Override
    public List<HorarioVeterinario> listar(Long medicoId) {
        if (medicoId == null) {
            throw new BadRequestException("El parámetro medicoId es obligatorio");
        }
        return horarioRepository.findAllByMedicoIdOrderByDiaSemanaAscHoraInicioAsc(medicoId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public HorarioVeterinario crear(HorarioRq rq) {
        validar(rq, null);
        HorarioVeterinario h = new HorarioVeterinario();
        aplicar(h, rq);
        return horarioRepository.save(h);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public HorarioVeterinario actualizar(HorarioRq rq) {
        if (rq == null || rq.getId() == null) {
            throw new BadRequestException("El id del horario es obligatorio");
        }
        HorarioVeterinario h = horarioRepository.findById(rq.getId())
                .orElseThrow(() -> new NotFoundException("El horario " + rq.getId() + " no existe"));
        validar(rq, h.getId());
        aplicar(h, rq);
        return horarioRepository.save(h);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!horarioRepository.existsById(id)) {
            throw new NotFoundException("El horario " + id + " no existe");
        }
        horarioRepository.deleteById(id);
    }

    private void validar(HorarioRq rq, Long excluir) {
        if (rq == null || rq.getMedicoId() == null || rq.getDiaSemana() == null
                || rq.getHoraInicio() == null || rq.getHoraFin() == null) {
            throw new BadRequestException("Médico, día, hora de inicio y hora de fin son obligatorios");
        }
        if (!medicoRepository.existsById(rq.getMedicoId())) {
            throw new BadRequestException("El médico " + rq.getMedicoId() + " no existe");
        }
        if (rq.getDiaSemana() < 1 || rq.getDiaSemana() > 7) {
            throw new BadRequestException("El día debe estar entre 1 (lunes) y 7 (domingo)");
        }
        if (!rq.getHoraFin().isAfter(rq.getHoraInicio())) {
            throw new BadRequestException("La hora de fin debe ser posterior a la hora de inicio");
        }
        boolean cruce = horarioRepository.findAllByMedicoIdAndDiaSemanaAndActivoTrue(rq.getMedicoId(), rq.getDiaSemana())
                .stream().filter(o -> excluir == null || !o.getId().equals(excluir))
                .anyMatch(o -> rq.getHoraInicio().isBefore(o.getHoraFin()) && rq.getHoraFin().isAfter(o.getHoraInicio()));
        if (cruce) {
            throw new BadRequestException("La franja se cruza con otra franja activa del mismo día");
        }
    }

    private void aplicar(HorarioVeterinario h, HorarioRq rq) {
        h.setMedicoId(rq.getMedicoId());
        h.setDiaSemana(rq.getDiaSemana());
        h.setHoraInicio(rq.getHoraInicio());
        h.setHoraFin(rq.getHoraFin());
        h.setActivo(rq.getActivo() == null ? Boolean.TRUE : rq.getActivo());
    }
}
