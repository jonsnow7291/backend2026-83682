package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.FacturaDtos.*;
import com.uniminuto.clinica.repository.*;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.service.FacturaService;
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
 * Implementación de facturación. Reglas: solo citas atendidas; una factura vigente por cita (no se
 * cobra dos veces); precios e IVA congelados en la línea; solo medicamentos efectivamente
 * despachados (y el despacho no puede quedar sin procesar); descuentos solo por ADMIN; la anulación conserva el histórico y exige que no existan pagos.
 */
@Service
public class FacturaServiceImpl implements FacturaService {

    @Autowired private FacturaRepository facturaRepository;
    @Autowired private PagoRepository pagoRepository;
    @Autowired private CitaRepository citaRepository;
    @Autowired private AtencionRepository atencionRepository;
    @Autowired private TarifaRepository tarifaRepository;
    @Autowired private EspecializacionRepository especializacionRepository;
    @Autowired private LineasFactura lineasFactura;
    @Autowired private FormulaMedicaRepository formulaRepository;
    @Autowired private DespachoRepository despachoRepository;
    @Autowired private UsuarioActual usuarioActual;

    /** {@inheritDoc} */
    @Override
    @Transactional
    public FacturaRs generar(GenerarRq rq) {
        if (rq == null || rq.citaId() == null) {
            throw new BadRequestException("La cita es obligatoria");
        }
        Cita cita = citaRepository.bloquear(rq.citaId())
                .orElseThrow(() -> new NotFoundException("La cita " + rq.citaId() + " no existe"));
        if (!CitaEstados.fueAtendida(cita.getEstado())) {
            throw new BadRequestException("Solo se facturan citas atendidas (estado actual: " + cita.getEstado() + ")");
        }
        if (facturaRepository.findFirstByCitaIdAndEstadoPagoNot(cita.getId(), "ANULADA").isPresent()) {
            throw new ConflictException("La cita ya tiene una factura vigente: no se puede cobrar dos veces el mismo servicio");
        }
        // RN-FAC: solo se factura lo efectivamente dispensado, así que el despacho de cada fórmula
        // confirmada debe haberse procesado antes de facturar (no puede seguir sin atender).
        for (FormulaMedica fm : formulaRepository.findAllByCita_IdOrderByIdAsc(cita.getId())) {
            Despacho dp = despachoRepository.findByFormulaId(fm.getId()).orElse(null);
            if (dp != null && ("PENDIENTE_DESPACHO".equals(dp.getEstado()) || "EN_PREPARACION".equals(dp.getEstado()))) {
                throw new ConflictException("La fórmula " + fm.getId() + " aún no ha sido despachada (" + dp.getEstado()
                        + "). Espere el despacho antes de facturar o solicite su cancelación autorizada");
            }
        }
        AtencionMedica atencion = atencionRepository.findByCitaId(cita.getId()).orElse(null);
        Tarifa tarifa = null;
        if (atencion != null && atencion.getTarifaId() != null) {
            tarifa = tarifaRepository.findById(atencion.getTarifaId()).orElse(null);
        }
        if (tarifa == null) {
            tarifa = tarifaRepository.findAllByEspecializacionIdAndActivaTrueOrderByIdAsc(cita.getMedico().getEspecializacionId().longValue())
                    .stream().findFirst().orElseThrow(() -> new BadRequestException("No hay una tarifa activa para la especialidad del veterinario"));
        }
        List<FacturaDetalle> lineas = new ArrayList<>();
        lineas.add(lineasFactura.servicio(tarifa));
        lineas.addAll(lineasFactura.medicamentos(cita.getId(), true));
        BigDecimal subtotal = lineasFactura.subtotal(lineas);
        BigDecimal impuestos = lineasFactura.impuestos(lineas);
        BigDecimal descuento = rq.descuento() == null ? BigDecimal.ZERO : rq.descuento();
        if (descuento.signum() < 0 || descuento.compareTo(subtotal.add(impuestos)) > 0) {
            throw new BadRequestException("El descuento no puede ser negativo ni superar el total de la factura");
        }
        // RN-FAC: los descuentos solo los autoriza un ADMIN (requerimiento 3.4 "descuentos, cuando estén autorizados").
        if (descuento.signum() > 0 && !usuarioActual.tieneRol(com.uniminuto.clinica.security.Roles.ADMIN)) {
            throw new org.springframework.security.access.AccessDeniedException("Solo un administrador puede autorizar descuentos");
        }
        Factura f = new Factura();
        f.setCitaId(cita.getId());
        f.setAtencionId(atencion == null ? null : atencion.getId());
        f.setClienteId(cita.getCliente().getClienteId());
        f.setMascotaId(cita.getMascota().getMascotaId());
        f.setMedicoId(cita.getMedico().getId());
        f.setEspecializacionId(cita.getMedico().getEspecializacionId().longValue());
        f.setSubtotal(subtotal);
        f.setImpuestos(impuestos);
        f.setDescuento(descuento);
        f.setTotal(subtotal.add(impuestos).subtract(descuento));
        f.setFecha(LocalDateTime.now());
        f.setEstadoPago(f.getTotal().signum() == 0 ? "PAGADA" : "PENDIENTE");
        f.setUsuarioId(usuarioActual.id());
        f.getDetalles().addAll(lineas);
        f = facturaRepository.save(f);
        f.setNumero(String.format("FAC-%d-%06d", f.getFecha().getYear(), f.getId()));
        facturaRepository.save(f);
        return armar(f.getId());
    }

    /** {@inheritDoc} */
    @Override
    public FacturaRs consultar(Long id) {
        return armar(id);
    }

    /** {@inheritDoc} */
    @Override
    public List<FacturaRs> listar(Long clienteId, Integer mascotaId) {
        List<Factura> base = mascotaId != null ? facturaRepository.findAllByMascotaIdOrderByFechaDesc(mascotaId)
                : clienteId != null ? facturaRepository.findAllByClienteIdOrderByFechaDesc(clienteId)
                : facturaRepository.findAllByOrderByFechaDesc();
        return base.stream().filter(f -> clienteId == null || f.getClienteId().equals(clienteId))
                .map(f -> armar(f.getId())).toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public FacturaRs anular(Long id, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new BadRequestException("El motivo de anulación es obligatorio");
        }
        Factura f = facturaRepository.bloquear(id).orElseThrow(() -> new NotFoundException("La factura " + id + " no existe"));
        if ("ANULADA".equals(f.getEstadoPago())) {
            throw new ConflictException("La factura ya está anulada");
        }
        if (pagoRepository.totalPagado(id).signum() > 0) {
            throw new ConflictException("La factura tiene pagos registrados y no puede anularse");
        }
        f.setEstadoPago("ANULADA");
        f.setMotivoAnulacion(motivo.trim());
        f.setFechaAnulacion(LocalDateTime.now());
        facturaRepository.save(f);
        return armar(id);
    }

    /** {@inheritDoc} */
    @Override
    public FacturaRs armar(Long id) {
        Factura f = facturaRepository.findById(id).orElseThrow(() -> new NotFoundException("La factura " + id + " no existe"));
        Cita c = citaRepository.findById(f.getCitaId()).orElseThrow();
        List<Pago> pagos = pagoRepository.findAllByFacturaIdOrderByFecha(id);
        BigDecimal pagado = pagos.stream().map(Pago::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        String esp = f.getEspecializacionId() == null ? "-" : especializacionRepository.findById(f.getEspecializacionId())
                .map(Especializacion::getNombre).orElse("-");
        return new FacturaRs(f, c.getCliente().getNombres() + " " + c.getCliente().getApellidos(),
                c.getMascota().getNombreMascota(), c.getMedico().getNombres() + " " + c.getMedico().getApellidos(), esp,
                pagos, pagado, f.getTotal().subtract(pagado));
    }

    /** {@inheritDoc} */
    @Override
    public byte[] pdf(Long id) {
        FacturaRs r = armar(id);
        Factura f = r.factura();
        List<List<Object>> filas = new ArrayList<>();
        for (FacturaDetalle d : f.getDetalles()) {
            filas.add(List.of(d.getTipo(), d.getDescripcion(), d.getCantidad(), d.getPrecioUnitario(),
                    d.getIvaPorcentaje() + "%", d.getSubtotal().add(d.getIvaValor())));
        }
        return new PdfBuilder("FACTURA " + f.getNumero(), false)
                .campo("Fecha", f.getFecha()).campo("Estado de pago", f.getEstadoPago())
                .campo("Propietario", r.propietario()).campo("Mascota", r.mascota())
                .campo("Veterinario", r.veterinario()).campo("Especialidad", r.especialidad())
                .campo("Cita", f.getCitaId())
                .seccion("Detalle")
                .tabla(List.of("Tipo", "Descripción", "Cant.", "Precio unit.", "IVA", "Total línea"), filas)
                .seccion("Totales")
                .campo("Subtotal", f.getSubtotal()).campo("Impuestos", f.getImpuestos())
                .campo("Descuento", f.getDescuento()).campo("TOTAL", f.getTotal())
                .campo("Pagado", r.totalPagado()).campo("Saldo", r.saldo())
                .campo("Anulación", f.getMotivoAnulacion())
                .build();
    }
}
