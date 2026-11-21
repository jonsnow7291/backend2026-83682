package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.EspecializacionRq;
import com.uniminuto.clinica.repository.EspecializacionRepository;
import com.uniminuto.clinica.service.EspecializacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de las operaciones de negocio sobre especializaciones médicas.
 */
@Service
public class EspecializacionServiceImpl implements EspecializacionService {

    /**
     * Repositorio de acceso a la tabla especializacion.
     */
    @Autowired
    private EspecializacionRepository especializacionRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Especializacion> obtenerEspecializaciones() {
        return especializacionRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Especializacion cambiarEstado(Long id, boolean activo) {
        Especializacion e = especializacionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("La especialidad " + id + " no existe"));
        e.setActiva(activo);
        return especializacionRepository.save(e);
    }

    /** {@inheritDoc} */
    @Override
    public Especializacion guardar(EspecializacionRq rq) {
        validar(rq, null);
        Especializacion e = new Especializacion();
        e.setActiva(true);
        return especializacionRepository.save(aplicar(e, rq));
    }

    /** {@inheritDoc} */
    @Override
    public Especializacion actualizar(EspecializacionRq rq) {
        if (rq == null || rq.getId() == null) {
            throw new BadRequestException("El id de la especialidad es obligatorio");
        }
        Especializacion e = especializacionRepository.findById(rq.getId())
                .orElseThrow(() -> new NotFoundException("La especialidad " + rq.getId() + " no existe"));
        validar(rq, e.getId());
        return especializacionRepository.save(aplicar(e, rq));
    }

    private Especializacion aplicar(Especializacion e, EspecializacionRq rq) {
        e.setNombre(rq.getNombre().trim());
        e.setCodigoEspecializacion(rq.getCodigoEspecializacion().trim().toUpperCase());
        e.setDescripcion(rq.getDescripcion());
        return e;
    }

    /** Nombre y código obligatorios, código corto y válido, y ambos únicos (RN-TAR). */
    private void validar(EspecializacionRq rq, Long idActual) {
        if (rq == null || rq.getNombre() == null || rq.getNombre().isBlank()) {
            throw new BadRequestException("El nombre de la especialidad es obligatorio");
        }
        if (rq.getNombre().trim().length() > 100) {
            throw new BadRequestException("El nombre no puede superar 100 caracteres");
        }
        String codigo = rq.getCodigoEspecializacion() == null ? "" : rq.getCodigoEspecializacion().trim();
        if (!codigo.matches("[A-Za-z0-9_-]{1,10}")) {
            throw new BadRequestException("El código debe tener entre 1 y 10 caracteres alfanuméricos (se admiten - y _)");
        }
        for (Especializacion otra : especializacionRepository.findAll()) {
            if (otra.getId().equals(idActual)) {
                continue;
            }
            if (otra.getNombre().equalsIgnoreCase(rq.getNombre().trim())) {
                throw new ConflictException("Ya existe una especialidad con el nombre '" + rq.getNombre().trim() + "'");
            }
            if (otra.getCodigoEspecializacion().equalsIgnoreCase(codigo)) {
                throw new ConflictException("Ya existe una especialidad con el código '" + codigo + "'");
            }
        }
    }
}
