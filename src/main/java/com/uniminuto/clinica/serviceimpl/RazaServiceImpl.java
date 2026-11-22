package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.RazaRq;
import com.uniminuto.clinica.repository.RazaRepository;
import com.uniminuto.clinica.service.RazaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación de {@link RazaService}.
 */
@Service
public class RazaServiceImpl implements RazaService {

    @Autowired
    private RazaRepository razaRepository;

    @Override
    public List<Raza> obtenerRazas() {
        return razaRepository.findAll();
    }

    @Override
    public Raza guardarRaza(RazaRq rq) {
        validar(rq, null);
        Raza r = new Raza();
        r.setNombre(rq.getNombre().trim());
        r.setEspecie(rq.getEspecie().trim());
        r.setFechaCreacion(LocalDateTime.now());
        return razaRepository.save(r);
    }

    @Override
    public Raza actualizarRaza(RazaRq rq) {
        if (rq == null || rq.getRazaId() == null || rq.getRazaId() <= 0) {
            throw new BadRequestException("El id de la raza es obligatorio");
        }
        Raza r = razaRepository.findById(rq.getRazaId())
                .orElseThrow(() -> new NotFoundException("La raza " + rq.getRazaId() + " no existe"));
        validar(rq, r.getRazaId());
        r.setNombre(rq.getNombre().trim());
        r.setEspecie(rq.getEspecie().trim());
        r.setFechaModificacion(LocalDateTime.now());
        return razaRepository.save(r);
    }

    private void validar(RazaRq rq, Integer idExcluir) {
        if (rq == null || rq.getNombre() == null || rq.getNombre().isBlank()) {
            throw new BadRequestException("El nombre de la raza es obligatorio");
        }
        if (rq.getEspecie() == null || rq.getEspecie().isBlank()) {
            throw new BadRequestException("La especie es obligatoria");
        }
        if (rq.getNombre().trim().length() > 100 || rq.getEspecie().trim().length() > 100) {
            throw new BadRequestException("Nombre y especie no pueden superar 100 caracteres");
        }
        if (rq.getNombre().contains("<") || rq.getNombre().contains(">")
                || rq.getEspecie().contains("<") || rq.getEspecie().contains(">")) {
            throw new BadRequestException("Nombre y especie contienen caracteres no permitidos (< >)");
        }
        boolean existe = razaRepository.findAll().stream()
                .anyMatch(x -> x.getNombre().equalsIgnoreCase(rq.getNombre().trim())
                        && x.getEspecie().equalsIgnoreCase(rq.getEspecie().trim())
                        && !x.getRazaId().equals(idExcluir));
        if (existe) {
            throw new ConflictException("Ya existe la raza '" + rq.getNombre().trim() + "' para la especie " + rq.getEspecie().trim());
        }
    }
}
