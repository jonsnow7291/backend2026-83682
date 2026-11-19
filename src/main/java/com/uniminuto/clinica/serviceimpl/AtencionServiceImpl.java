package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AtencionMedica;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.FacturaDetalle;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Tarifa;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.AtencionRq;
import com.uniminuto.clinica.repository.AtencionRepository;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.TarifaRepository;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.service.AtencionService;
import com.uniminuto.clinica.service.LineasFactura;
import com.uniminuto.clinica.util.CitaEstados;
import com.uniminuto.clinica.util.PdfBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de la atención médica. Un MEDICO solo atiende sus propias citas.
 */
@Service
public class AtencionServiceImpl implements AtencionService {

    @Autowired
    private AtencionRepository atencionRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private TarifaRepository tarifaRepository;

    @Autowired
    private HistoriaMedicaRepository historiaRepository;

    @Autowired
    private LineasFactura lineasFactura;

    @Autowired
    private UsuarioActual usuarioActual;

    /** {@inheritDoc} */
    @Override
    @Transactional
    public AtencionMedica abrir(AtencionRq rq) {
        if (rq == null || rq.getCitaId() == null) {
            throw new BadRequestException("La cita es obligatoria");
        }
        Cita cita = citaRepository.bloquear(rq.getCitaId())
                .orElseThrow(() -> new NotFoundException("La cita " + rq.getCitaId() + " no existe"));
        usuarioActual.exigirPropioSiEsMedico(cita.getMedico().getId());
        if (atencionRepository.findByCitaId(cita.getId()).isPresent()) {
            throw new ConflictException("La cita ya tiene una atención registrada");
        }
        if (!List.of(CitaEstados.PENDIENTE, CitaEstados.CONFIRMADA, CitaEstados.ATENDIDA).contains(cita.getEstado())) {
            throw new BadRequestException("No se puede abrir la atención de una cita en estado " + cita.getEstado());
        }
        AtencionMedica a = new AtencionMedica();
        a.setCitaId(cita.getId());
        a.setMedicoId(cita.getMedico().getId());
        a.setTarifaId(resolverTarifa(rq.getTarifaId(), cita));
        a.setMotivo(rq.getMotivo() != null ? rq.getMotivo() : cita.getMotivo());
        a.setSintomas(rq.getSintomas());
        a.setDiagnostico(rq.getDiagnostico());
        a.setObservaciones(rq.getObservaciones());
        a.setRecomendaciones(rq.getRecomendaciones());
        a.setEstado("EN_CURSO");
        a.setFechaInicio(LocalDateTime.now());
        cita.setEstado(CitaEstados.ATENDIDA);
        citaRepository.save(cita);
        return atencionRepository.save(a);
    }

    private Long resolverTarifa(Long tarifaId, Cita cita) {
        if (tarifaId != null) {
            Tarifa t = tarifaRepository.findById(tarifaId)
                    .orElseThrow(() -> new BadRequestException("La tarifa " + tarifaId + " no existe"));
            if (!Boolean.TRUE.equals(t.getActiva())) {
                throw new BadRequestException("La tarifa seleccionada está inactiva");
            }
            return t.getId();
        }
        List<Tarifa> tarifas = tarifaRepository.findAllByEspecializacionIdAndActivaTrueOrderByIdAsc(cita.getMedico().getEspecializacionId().longValue());
        return tarifas.stream().filter(t -> t.getServicio().toLowerCase().contains("consulta")).findFirst()
                .or(() -> tarifas.stream().findFirst()).map(Tarifa::getId).orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public AtencionMedica registrar(AtencionRq rq) {
        AtencionMedica a = cargar(rq == null ? null : rq.getId());
        if (!"EN_CURSO".equals(a.getEstado())) {
            throw new BadRequestException("La atención ya está finalizada y no admite cambios");
        }
        if (rq.getMotivo() != null) a.setMotivo(rq.getMotivo());
        if (rq.getSintomas() != null) a.setSintomas(rq.getSintomas());
        if (rq.getDiagnostico() != null) a.setDiagnostico(rq.getDiagnostico());
        if (rq.getObservaciones() != null) a.setObservaciones(rq.getObservaciones());
        if (rq.getRecomendaciones() != null) a.setRecomendaciones(rq.getRecomendaciones());
        if (rq.getTarifaId() != null) a.setTarifaId(resolverTarifa(rq.getTarifaId(), citaRepository.findById(a.getCitaId()).orElseThrow()));
        return atencionRepository.save(a);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public AtencionMedica finalizar(Long id) {
        AtencionMedica a = cargar(id);
        if (!"EN_CURSO".equals(a.getEstado())) {
            throw new ConflictException("La atención ya fue finalizada");
        }
        if (a.getDiagnostico() == null || a.getDiagnostico().isBlank()) {
            throw new BadRequestException("Debe registrar el diagnóstico antes de finalizar la atención");
        }
        a.setEstado("FINALIZADA");
        a.setFechaFin(LocalDateTime.now());
        Cita cita = citaRepository.findById(a.getCitaId()).orElseThrow();
        cita.setEstado(CitaEstados.FINALIZADA);
        citaRepository.save(cita);
        if (historiaRepository.findAllByCita_Id(cita.getId()).isEmpty()) {
            HistoriaMedica h = new HistoriaMedica();
            h.setMascota(cita.getMascota());
            h.setCita(cita);
            h.setDiagnostico(a.getDiagnostico());
            h.setObservaciones(String.join(" | ", textos(a)));
            h.setFechaCreacion(LocalDateTime.now());
            historiaRepository.save(h);
        }
        return atencionRepository.save(a);
    }

    private List<String> textos(AtencionMedica a) {
        List<String> t = new ArrayList<>();
        if (a.getSintomas() != null && !a.getSintomas().isBlank()) t.add("Síntomas: " + a.getSintomas());
        if (a.getObservaciones() != null && !a.getObservaciones().isBlank()) t.add("Observaciones: " + a.getObservaciones());
        if (a.getRecomendaciones() != null && !a.getRecomendaciones().isBlank()) t.add("Recomendaciones: " + a.getRecomendaciones());
        return t;
    }

    /** {@inheritDoc} */
    @Override
    public AtencionMedica consultar(Long id) {
        return cargar(id);
    }

    /** {@inheritDoc} */
    @Override
    public List<AtencionMedica> historialMascota(Integer mascotaId) {
        List<AtencionMedica> todas = atencionRepository.findAllByOrderByFechaInicioDesc();
        return todas.stream().filter(a -> citaRepository.findById(a.getCitaId())
                .map(c -> c.getMascota().getMascotaId().equals(mascotaId)).orElse(false)).toList();
    }

    /** {@inheritDoc} */
    @Override
    public byte[] prefacturaPdf(Long id) {
        AtencionMedica a = cargar(id);
        Cita c = citaRepository.findById(a.getCitaId()).orElseThrow();
        List<FacturaDetalle> lineas = new ArrayList<>();
        if (a.getTarifaId() != null) {
            lineas.add(lineasFactura.servicio(tarifaRepository.findById(a.getTarifaId()).orElseThrow()));
        }
        lineas.addAll(lineasFactura.medicamentos(c.getId(), false));
        List<List<Object>> filas = new ArrayList<>();
        for (FacturaDetalle l : lineas) {
            filas.add(List.of(l.getTipo(), l.getDescripcion(), l.getCantidad(), l.getPrecioUnitario(), l.getIvaPorcentaje() + "%", l.getSubtotal().add(l.getIvaValor())));
        }
        BigDecimal sub = lineasFactura.subtotal(lineas);
        BigDecimal iva = lineasFactura.impuestos(lineas);
        return new PdfBuilder("PREFACTURA (valores estimados, no es una factura)", false)
                .campo("Atención", a.getId()).campo("Cita", c.getId())
                .campo("Propietario", c.getCliente().getNombres() + " " + c.getCliente().getApellidos())
                .campo("Mascota", c.getMascota().getNombreMascota())
                .campo("Veterinario", c.getMedico().getNombres() + " " + c.getMedico().getApellidos())
                .campo("Diagnóstico", a.getDiagnostico())
                .seccion("Servicios y medicamentos formulados")
                .tabla(List.of("Tipo", "Descripción", "Cant.", "Precio unit.", "IVA", "Total línea"), filas)
                .seccion("Totales estimados")
                .campo("Subtotal", sub).campo("IVA", iva).campo("Total estimado", sub.add(iva))
                .texto("Los medicamentos se facturarán según lo efectivamente despachado.")
                .build();
    }

    private AtencionMedica cargar(Long id) {
        if (id == null) {
            throw new BadRequestException("El id de la atención es obligatorio");
        }
        AtencionMedica a = atencionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("La atención " + id + " no existe"));
        usuarioActual.exigirPropioSiEsMedico(a.getMedicoId());
        return a;
    }
}
