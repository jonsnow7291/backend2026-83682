package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.FormulaMedicaDetalle;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.AtencionRepository;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.DespachoRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.LoteRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.service.DespachoService;
import com.uniminuto.clinica.service.FormulaMedicaService;
import com.uniminuto.clinica.util.CitaEstados;
import com.uniminuto.clinica.util.PdfBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación de fórmulas médicas. Reglas: solo sobre citas atendidas; el médico de la cita
 * (o un ADMIN) la emite en BORRADOR y solo el MEDICO la confirma (regla 5.8-2), momento en que nace
 * su despacho; solo medicamentos activos de tipo MEDICAMENTO; crear/editar/confirmar no toca el
 * inventario; no se edita una vez que el despacho dejó de estar PENDIENTE_DESPACHO. La cantidad
 * prescrita se calcula como unidades por dosis × dosis por día × duración cuando se informan.
 */
@Service
public class FormulaMedicaServiceImpl implements FormulaMedicaService {

    @Autowired private FormulaMedicaRepository formulaRepository;
    @Autowired private CitaRepository citaRepository;
    @Autowired private MedicamentoRepository medicamentoRepository;
    @Autowired private AtencionRepository atencionRepository;
    @Autowired private DespachoRepository despachoRepository;
    @Autowired private LoteRepository loteRepository;
    @Autowired private DespachoService despachoService;
    @Autowired private UsuarioActual usuarioActual;

    /** {@inheritDoc} */
    @Override
    public List<FormulaMedica> listarFormulasMedicas(Long citaId, Integer mascotaId) {
        List<FormulaMedica> base = citaId != null ? formulaRepository.findAllByCita_IdOrderByIdAsc(citaId)
                : mascotaId != null ? formulaRepository.findAllByCita_Mascota_MascotaIdOrderByFechaEmisionDesc(mascotaId)
                : formulaRepository.findAllByOrderByFechaCreacionRegistroDesc();
        return base.stream().filter(this::visible).toList();
    }

    private boolean visible(FormulaMedica f) {
        try {
            usuarioActual.exigirPropioSiEsMedico(f.getCita().getMedico().getId());
            return true;
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return false;
        } catch (RuntimeException e) {
            return true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public FormulaMedica consultarFormulaMedica(Long formulaId) throws BadRequestException {
        if (formulaId == null || formulaId <= 0) {
            throw new BadRequestException("El identificador de la fórmula es obligatorio");
        }
        FormulaMedica f = formulaRepository.findById(formulaId)
                .orElseThrow(() -> new NotFoundException("La fórmula " + formulaId + " no existe"));
        usuarioActual.exigirPropioSiEsMedico(f.getCita().getMedico().getId());
        return f;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS guardarFormulaMedica(FormulaMedicaRq rq) throws BadRequestException {
        if (rq == null || rq.getCitaId() == null) {
            throw new BadRequestException("La cita es obligatoria");
        }
        Cita cita = citaRepository.findById(rq.getCitaId())
                .orElseThrow(() -> new NotFoundException("La cita " + rq.getCitaId() + " no existe"));
        usuarioActual.exigirPropioSiEsMedico(cita.getMedico().getId());
        if (!CitaEstados.fueAtendida(cita.getEstado())) {
            throw new BadRequestException("Solo se pueden emitir fórmulas sobre citas en estado ATENDIDA o FINALIZADA");
        }
        FormulaMedica f = new FormulaMedica();
        f.setCita(cita);
        f.setMedicoId(cita.getMedico().getId());
        f.setAtencionId(atencionRepository.findByCitaId(cita.getId()).map(a -> a.getId()).orElse(null));
        f.setIndicaciones(rq.getIndicaciones());
        f.setUsuarioId(usuarioActual.id());
        f.setFechaEmision(LocalDateTime.now());
        f.setFechaCreacionRegistro(LocalDateTime.now());
        cargarDetalles(f, rq);
        f = formulaRepository.save(f);
        MiRespuestaRS r = new MiRespuestaRS();
        r.setStatus(200);
        r.setMessage("Fórmula médica registrada exitosamente en borrador. Id: " + f.getId()
                + ". Debe confirmarla para que pase a despacho.");
        return r;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS confirmarFormulaMedica(Long formulaId) throws BadRequestException {
        FormulaMedica f = consultarFormulaMedica(formulaId);
        if (!"BORRADOR".equals(f.getEstado())) {
            throw new BadRequestException("La fórmula " + formulaId + " ya fue confirmada");
        }
        if (f.getDetalles().isEmpty()) {
            throw new BadRequestException("La fórmula debe incluir al menos un medicamento");
        }
        f.setEstado("CONFIRMADA");
        f.setFechaConfirmacion(LocalDateTime.now());
        f.setEstadoDespacho("PENDIENTE_DESPACHO");
        f.setFechaActualizacionRegistro(LocalDateTime.now());
        f = formulaRepository.save(f);
        despachoService.crearParaFormula(f);
        MiRespuestaRS r = new MiRespuestaRS();
        r.setStatus(200);
        r.setMessage("Fórmula médica confirmada. Quedó pendiente de despacho.");
        return r;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS actualizarFormulaMedica(FormulaMedicaRq rq) throws BadRequestException {
        if (rq == null || rq.getFormulaId() == null) {
            throw new BadRequestException("El id de la fórmula es obligatorio");
        }
        FormulaMedica f = consultarFormulaMedica(rq.getFormulaId());
        var despacho = despachoRepository.findByFormulaId(f.getId()).orElse(null);
        if (despacho != null && !"PENDIENTE_DESPACHO".equals(despacho.getEstado())) {
            throw new BadRequestException("La fórmula ya está en proceso de despacho (" + despacho.getEstado() + ") y no puede modificarse");
        }
        f.setIndicaciones(rq.getIndicaciones());
        f.getDetalles().clear();
        formulaRepository.saveAndFlush(f);
        cargarDetalles(f, rq);
        f.setFechaActualizacionRegistro(LocalDateTime.now());
        f = formulaRepository.saveAndFlush(f);
        if (despacho != null) {
            despachoService.sincronizarConFormula(f);
        }
        MiRespuestaRS r = new MiRespuestaRS();
        r.setStatus(200);
        r.setMessage("Fórmula médica actualizada exitosamente.");
        return r;
    }

    private void cargarDetalles(FormulaMedica f, FormulaMedicaRq rq) {
        if (rq.getDetalles() == null || rq.getDetalles().isEmpty()) {
            throw new BadRequestException("La fórmula debe incluir al menos un medicamento");
        }
        List<Long> vistos = new ArrayList<>();
        for (FormulaMedicaRq.Detalle d : rq.getDetalles()) {
            if (d.getMedicamentoId() == null) {
                throw new BadRequestException("Cada línea requiere un medicamento");
            }
            if (vistos.contains(d.getMedicamentoId())) {
                throw new BadRequestException("El medicamento " + d.getMedicamentoId() + " está repetido en la fórmula");
            }
            vistos.add(d.getMedicamentoId());
            Medicamento m = medicamentoRepository.findById(d.getMedicamentoId())
                    .orElseThrow(() -> new BadRequestException("El medicamento " + d.getMedicamentoId() + " no existe"));
            if (!"MEDICAMENTO".equals(m.getTipo()) || !Boolean.TRUE.equals(m.getActivo())) {
                throw new BadRequestException("'" + m.getNombre() + "' no es un medicamento activo que pueda formularse");
            }
            if (d.getDosis() == null || d.getDosis().isBlank() || d.getFrecuencia() == null || d.getFrecuencia().isBlank()) {
                throw new BadRequestException("Dosis y frecuencia son obligatorias para " + m.getNombre());
            }
            if (d.getDuracionDias() == null || d.getDuracionDias() <= 0) {
                throw new BadRequestException("La duración debe ser mayor que cero para " + m.getNombre());
            }
            Integer cantidad = d.getCantidadPrescrita();
            if (d.getUnidadesPorDosis() != null || d.getDosisPorDia() != null) {
                if (d.getUnidadesPorDosis() == null || d.getUnidadesPorDosis() <= 0
                        || d.getDosisPorDia() == null || d.getDosisPorDia() <= 0) {
                    throw new BadRequestException("Unidades por dosis y dosis por día deben ser mayores que cero para " + m.getNombre());
                }
                // Cantidad total requerida según la formulación (requerimiento 1.3).
                cantidad = d.getUnidadesPorDosis() * d.getDosisPorDia() * d.getDuracionDias();
            }
            if (cantidad == null || cantidad <= 0) {
                throw new BadRequestException("La cantidad prescrita debe ser mayor que cero para " + m.getNombre());
            }
            FormulaMedicaDetalle fd = new FormulaMedicaDetalle();
            fd.setMedicamento(m);
            fd.setDosis(d.getDosis().trim());
            fd.setFrecuencia(d.getFrecuencia().trim());
            fd.setDuracionDias(d.getDuracionDias());
            fd.setUnidadesPorDosis(d.getUnidadesPorDosis());
            fd.setDosisPorDia(d.getDosisPorDia());
            fd.setCantidadPrescrita(cantidad);
            fd.setViaAdministracion(d.getViaAdministracion());
            fd.setIndicaciones(d.getIndicaciones());
            f.getDetalles().add(fd);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Map<String, Object>> disponibilidad(Long formulaId) {
        FormulaMedica f = consultarFormulaMedica(formulaId);
        List<Map<String, Object>> r = new ArrayList<>();
        for (FormulaMedicaDetalle d : f.getDetalles()) {
            long stock = loteRepository.stockVigente(d.getMedicamento().getId(), LocalDate.now());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("medicamentoId", d.getMedicamento().getId());
            m.put("medicamento", d.getMedicamento().getNombre());
            m.put("cantidadPrescrita", d.getCantidadPrescrita());
            m.put("stockVigente", stock);
            m.put("disponible", stock >= d.getCantidadPrescrita());
            r.add(m);
        }
        return r;
    }

    /** {@inheritDoc} */
    @Override
    public byte[] pdf(Long formulaId) {
        FormulaMedica f = consultarFormulaMedica(formulaId);
        Cita c = f.getCita();
        List<List<Object>> filas = new ArrayList<>();
        for (FormulaMedicaDetalle d : f.getDetalles()) {
            filas.add(List.of(d.getMedicamento().getNombre(), d.getDosis(), d.getFrecuencia(), d.getDuracionDias() + " días",
                    d.getCantidadPrescrita(), d.getViaAdministracion() == null ? "-" : d.getViaAdministracion(),
                    d.getIndicaciones() == null ? "" : d.getIndicaciones()));
        }
        return new PdfBuilder("FÓRMULA MÉDICA No. " + f.getId(), true)
                .campo("Fecha de emisión", f.getFechaEmision().toLocalDate())
                .campo("Propietario", c.getCliente().getNombres() + " " + c.getCliente().getApellidos())
                .campo("Mascota", c.getMascota().getNombreMascota())
                .campo("Veterinario", c.getMedico().getNombres() + " " + c.getMedico().getApellidos())
                .campo("Registro profesional", c.getMedico().getRegistroProfesional())
                .campo("Cita", c.getId())
                .seccion("Medicamentos")
                .tabla(List.of("Medicamento", "Dosis", "Frecuencia", "Duración", "Cantidad", "Vía", "Indicaciones"), filas)
                .seccion("Indicaciones generales").texto(f.getIndicaciones())
                .build();
    }
}
