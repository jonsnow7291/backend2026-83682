package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.exception.UnauthorizedException;
import com.uniminuto.clinica.models.DespachoDtos.*;
import com.uniminuto.clinica.repository.*;
import com.uniminuto.clinica.security.Roles;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.service.DespachoService;
import com.uniminuto.clinica.service.InventarioService;
import com.uniminuto.clinica.util.PdfBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementación del despacho. Reglas del enunciado (5.8) y dónde se validan:
 * (1) perfil exclusivo: SecurityConfig + {@code @PreAuthorize}; (2) la fórmula la crea el médico:
 * {@code FormulaMedicaServiceImpl}; (3) el despacho no modifica dosis ni cantidades: las líneas
 * solo permiten entregar hasta lo solicitado; (4) el inventario solo se descuenta en {@link #confirmar};
 * (5) no se despachan lotes vencidos (FEFO solo vigentes); (6) no se entrega más de lo disponible;
 * (7) la entrega parcial está permitida; (8) toda salida lleva despacho y fórmula; (9) no hay doble
 * descuento: bloqueo del despacho + estado terminal DESPACHADO; (10) usuario, fecha y hora en toda
 * operación; (11) los despachos confirmados no se eliminan, solo se reversan con autorización;
 * (12) permisos validados en el backend.
 */
@Service
public class DespachoServiceImpl implements DespachoService {

    private static final List<String> PENDIENTES = List.of("PENDIENTE_DESPACHO", "EN_PREPARACION",
            "DESPACHO_PARCIAL", "PENDIENTE_DISPONIBILIDAD");
    private static final int DIAS_ALERTA = MedicamentoServiceImpl.DIAS_ALERTA_VENCIMIENTO;

    @Autowired private DespachoRepository despachoRepository;
    @Autowired private DespachoEstadoLogRepository logRepository;
    @Autowired private FormulaMedicaRepository formulaRepository;
    @Autowired private MedicamentoRepository medicamentoRepository;
    @Autowired private LoteRepository loteRepository;
    @Autowired private MovimientoRepository movimientoRepository;
    @Autowired private InventarioService inventarioService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UsuarioActual usuarioActual;

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Despacho crearParaFormula(FormulaMedica formula) {
        Despacho d = new Despacho();
        d.setFormulaId(formula.getId());
        d.setEstado("PENDIENTE_DESPACHO");
        d.setFechaCreacion(LocalDateTime.now());
        copiarLineas(d, formula);
        d = despachoRepository.save(d);
        log(d, null, d.getEstado(), "Fórmula emitida por el médico");
        return d;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void sincronizarConFormula(FormulaMedica formula) {
        Despacho d = despachoRepository.findByFormulaId(formula.getId()).orElseThrow();
        d.getDetalles().clear();
        despachoRepository.saveAndFlush(d);
        copiarLineas(d, formula);
        despachoRepository.save(d);
    }

    private void copiarLineas(Despacho d, FormulaMedica f) {
        for (FormulaMedicaDetalle fd : f.getDetalles()) {
            DespachoDetalle dd = new DespachoDetalle();
            dd.setFormulaDetalleId(fd.getId());
            dd.setMedicamentoId(fd.getMedicamento().getId());
            dd.setCantidadSolicitada(fd.getCantidadPrescrita());
            dd.setCantidadEntregada(0);
            d.getDetalles().add(dd);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Resumen> pendientes(String q, LocalDate fecha) {
        String t = q == null || q.isBlank() ? null : q.trim().toLowerCase();
        return despachoRepository.findAllByEstadoInOrderByFechaCreacionAsc(PENDIENTES).stream()
                .map(this::resumen)
                .filter(r -> fecha == null || r.fecha().toLocalDate().equals(fecha))
                .filter(r -> t == null || String.valueOf(r.formulaId()).equals(t)
                        || r.propietario().toLowerCase().contains(t) || r.mascota().toLowerCase().contains(t))
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public Detalle detallePorFormula(Long formulaId) {
        Despacho d = despachoRepository.findByFormulaId(formulaId)
                .orElseThrow(() -> new NotFoundException("La fórmula " + formulaId + " no tiene despacho"));
        return detalle(d);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Sugerencia preparar(Long id) {
        Despacho d = bloquear(id);
        if (!List.of("PENDIENTE_DESPACHO", "PENDIENTE_DISPONIBILIDAD", "DESPACHO_PARCIAL").contains(d.getEstado())) {
            throw new ConflictException("El despacho está en estado " + d.getEstado() + " y no puede pasar a preparación");
        }
        cambiarEstado(d, "EN_PREPARACION", "Inicio de preparación");
        d.setFechaPreparacion(LocalDateTime.now());
        despachoRepository.save(d);
        return sugerir(id);
    }

    /** {@inheritDoc} */
    @Override
    public Sugerencia sugerir(Long id) {
        Despacho d = despachoRepository.findById(id).orElseThrow(() -> new NotFoundException("El despacho " + id + " no existe"));
        List<Asignacion> asig = new ArrayList<>();
        List<String> faltantes = new ArrayList<>();
        for (DespachoDetalle dd : d.getDetalles()) {
            int pendiente = dd.getCantidadSolicitada() - dd.getCantidadEntregada();
            Medicamento m = medicamentoRepository.findById(dd.getMedicamentoId()).orElseThrow();
            for (LoteMedicamento l : loteRepository.lotesFefo(m.getId(), LocalDate.now())) {
                if (pendiente <= 0) break;
                int tomar = Math.min(pendiente, l.getCantidadDisponible());
                asig.add(new Asignacion(dd.getId(), m.getNombre(), l.getId(), l.getNumeroLote(), tomar, l.getFechaVencimiento()));
                pendiente -= tomar;
            }
            if (pendiente > 0) {
                faltantes.add(m.getNombre() + ": faltan " + pendiente + " unidades");
            }
        }
        return new Sugerencia(d.getId(), asig, faltantes);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Detalle confirmar(Long id, ConfirmarRq rq) {
        Despacho d = bloquear(id);
        if ("DESPACHADO".equals(d.getEstado()) || "CANCELADO".equals(d.getEstado())) {
            throw new ConflictException("El despacho ya está " + d.getEstado() + ": no se puede descontar nuevamente");
        }
        Map<Long, Integer> pedidas = new HashMap<>();
        if (rq != null && rq.lineas() != null) {
            for (LineaEntrega le : rq.lineas()) {
                if (le.detalleId() == null || le.cantidad() == null || le.cantidad() < 0) {
                    throw new BadRequestException("Cada línea requiere detalleId y una cantidad no negativa");
                }
                pedidas.put(le.detalleId(), le.cantidad());
            }
        }
        int entregadoAhora = 0;
        for (DespachoDetalle dd : d.getDetalles()) {
            int pendiente = dd.getCantidadSolicitada() - dd.getCantidadEntregada();
            if (pendiente <= 0) continue;
            Medicamento m = medicamentoRepository.findById(dd.getMedicamentoId()).orElseThrow();
            List<LoteMedicamento> lotes = loteRepository.lotesFefoBloqueados(m.getId(), LocalDate.now());
            int disponible = lotes.stream().mapToInt(LoteMedicamento::getCantidadDisponible).sum();
            Integer pedida = pedidas.get(dd.getId());
            int entregar;
            if (pedida == null) {
                entregar = rq != null && rq.lineas() != null && !rq.lineas().isEmpty() ? 0 : Math.min(pendiente, disponible);
            } else {
                if (pedida > pendiente) {
                    throw new BadRequestException("No se puede entregar más de lo solicitado por el médico para " + m.getNombre()
                            + " (pendiente " + pendiente + ")");
                }
                if (pedida > disponible) {
                    throw new BadRequestException("No hay existencias vigentes suficientes de " + m.getNombre()
                            + " (disponible " + disponible + ", solicitado " + pedida + ")");
                }
                entregar = pedida;
            }
            int restante = entregar;
            for (LoteMedicamento l : lotes) {
                if (restante <= 0) break;
                int tomar = Math.min(restante, l.getCantidadDisponible());
                inventarioService.registrarSalida(l.getId(), tomar, d.getFormulaId(), d.getId(),
                        "Despacho " + d.getId() + " de fórmula " + d.getFormulaId());
                restante -= tomar;
            }
            dd.setCantidadEntregada(dd.getCantidadEntregada() + entregar);
            entregadoAhora += entregar;
        }
        boolean completo = d.getDetalles().stream().allMatch(x -> x.getCantidadEntregada() >= x.getCantidadSolicitada());
        boolean algo = d.getDetalles().stream().anyMatch(x -> x.getCantidadEntregada() > 0);
        String nuevo = completo ? "DESPACHADO" : algo ? "DESPACHO_PARCIAL" : "PENDIENTE_DISPONIBILIDAD";
        d.setUsuarioId(usuarioActual.id());
        d.setFechaConfirmacion(LocalDateTime.now());
        if (rq != null && rq.observaciones() != null) {
            d.setObservaciones(rq.observaciones());
        }
        cambiarEstado(d, nuevo, "Confirmación: entregadas " + entregadoAhora + " unidades");
        despachoRepository.save(d);
        return detalle(d);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Resumen cancelar(Long id, AutorizacionRq rq) {
        String autorizador = validarAutorizacion(rq);
        Despacho d = bloquear(id);
        if (d.getDetalles().stream().anyMatch(x -> x.getCantidadEntregada() > 0) || "DESPACHADO".equals(d.getEstado())) {
            throw new ConflictException("El despacho ya entregó unidades: use la reversión autorizada");
        }
        if ("CANCELADO".equals(d.getEstado())) {
            throw new ConflictException("El despacho ya está cancelado");
        }
        d.setUsuarioId(usuarioActual.id());
        cambiarEstado(d, "CANCELADO", rq.motivo() + " (autorizado por " + autorizador + ")");
        despachoRepository.save(d);
        return resumen(d);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Resumen reversar(Long id, AutorizacionRq rq) {
        String autorizador = validarAutorizacion(rq);
        Despacho d = bloquear(id);
        if (d.getDetalles().stream().noneMatch(x -> x.getCantidadEntregada() > 0)) {
            throw new ConflictException("El despacho no tiene entregas para reversar");
        }
        Map<Long, Integer> neto = new LinkedHashMap<>();
        for (MovimientoInventario mv : movimientoRepository.findAllByDespachoId(d.getId())) {
            neto.merge(mv.getLoteId(), "SALIDA".equals(mv.getTipo()) ? mv.getCantidad() : -mv.getCantidad(), Integer::sum);
        }
        neto.forEach((loteId, cant) -> {
            if (cant > 0) {
                inventarioService.registrarReversion(loteId, cant, d.getFormulaId(), d.getId(),
                        "Reversión del despacho " + d.getId() + " (autorizado por " + autorizador + ")");
            }
        });
        d.getDetalles().forEach(x -> x.setCantidadEntregada(0));
        d.setUsuarioId(usuarioActual.id());
        cambiarEstado(d, "PENDIENTE_DESPACHO", "REVERSIÓN: " + rq.motivo() + " (autorizado por " + autorizador + ")");
        despachoRepository.save(d);
        return resumen(d);
    }

    /** Valida motivo y credenciales de un ADMIN autorizador; devuelve su username. */
    private String validarAutorizacion(AutorizacionRq rq) {
        if (rq == null || rq.motivo() == null || rq.motivo().isBlank()) {
            throw new BadRequestException("El motivo es obligatorio");
        }
        if (rq.autorizadorUsername() == null || rq.autorizadorPassword() == null) {
            throw new BadRequestException("Se requiere la autorización de un administrador (usuario y contraseña)");
        }
        Usuario a = usuarioRepository.findByUsername(rq.autorizadorUsername().trim()).orElse(null);
        if (a == null || !Boolean.TRUE.equals(a.getActivo()) || !Roles.ADMIN.equals(a.getRol())
                || a.getPasswordHash() == null || !passwordEncoder.matches(rq.autorizadorPassword(), a.getPasswordHash())) {
            throw new UnauthorizedException("Autorización inválida: se requiere un administrador activo con credenciales correctas");
        }
        return a.getUsername();
    }

    /** {@inheritDoc} */
    @Override
    public byte[] comprobantePdf(Long id) {
        Despacho d = despachoRepository.findById(id).orElseThrow(() -> new NotFoundException("El despacho " + id + " no existe"));
        if (d.getDetalles().stream().noneMatch(x -> x.getCantidadEntregada() > 0)) {
            throw new BadRequestException("El despacho aún no tiene entregas: no hay comprobante");
        }
        Resumen r = resumen(d);
        Map<Long, Integer> neto = new LinkedHashMap<>();
        for (MovimientoInventario mv : movimientoRepository.findAllByDespachoId(d.getId())) {
            neto.merge(mv.getLoteId(), "SALIDA".equals(mv.getTipo()) ? mv.getCantidad() : -mv.getCantidad(), Integer::sum);
        }
        List<List<Object>> filas = new ArrayList<>();
        neto.forEach((loteId, cant) -> {
            if (cant > 0) {
                LoteMedicamento l = loteRepository.findById(loteId).orElseThrow();
                Medicamento m = medicamentoRepository.findById(l.getMedicamentoId()).orElseThrow();
                filas.add(List.of(m.getNombre(), l.getNumeroLote(), l.getFechaVencimiento() == null ? "Sin vencimiento" : l.getFechaVencimiento().toString(), cant));
            }
        });
        String usuario = d.getUsuarioId() == null ? "-" : usuarioRepository.findById(d.getUsuarioId()).map(Usuario::getUsername).orElse("-");
        return new PdfBuilder("COMPROBANTE DE DESPACHO", false)
                .campo("Despacho", d.getId()).campo("Fórmula", d.getFormulaId()).campo("Estado", d.getEstado())
                .campo("Propietario", r.propietario()).campo("Mascota", r.mascota()).campo("Veterinario", r.medico())
                .campo("Despachado por", usuario).campo("Fecha y hora", d.getFechaConfirmacion())
                .seccion("Medicamentos entregados")
                .tabla(List.of("Medicamento", "Lote", "Vence", "Cantidad"), filas)
                .seccion("Solicitado vs. entregado")
                .tabla(List.of("Medicamento", "Solicitado", "Entregado"), d.getDetalles().stream().map(x -> List.<Object>of(
                        medicamentoRepository.findById(x.getMedicamentoId()).orElseThrow().getNombre(),
                        x.getCantidadSolicitada(), x.getCantidadEntregada())).toList())
                .build();
    }

    /** {@inheritDoc} */
    @Override
    public List<Resumen> historial() {
        return despachoRepository.findAllByOrderByFechaCreacionDesc().stream().map(this::resumen).toList();
    }

    /** {@inheritDoc} */
    @Override
    public List<MovimientoDespacho> historialMedicamento(Long medicamentoId) {
        return movimientoRepository.findAllByMedicamentoIdOrderByFechaDescIdDesc(medicamentoId).stream()
                .filter(m -> m.getDespachoId() != null)
                .map(m -> new MovimientoDespacho(m.getId(), m.getDespachoId(), m.getFormulaMedicaId(), m.getTipo(),
                        m.getCantidad(), m.getLoteId() == null ? null : loteRepository.findById(m.getLoteId())
                        .map(LoteMedicamento::getNumeroLote).orElse(null), m.getFecha(), m.getUsuarioId(), m.getMotivo()))
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public List<DespachoEstadoLog> estados(Long id) {
        return logRepository.findAllByDespachoIdOrderByFechaAscIdAsc(id);
    }

    // ------------------------------------------------------------------ utilidades

    private Despacho bloquear(Long id) {
        return despachoRepository.bloquear(id).orElseThrow(() -> new NotFoundException("El despacho " + id + " no existe"));
    }

    private void cambiarEstado(Despacho d, String nuevo, String motivo) {
        String anterior = d.getEstado();
        d.setEstado(nuevo);
        formulaRepository.findById(d.getFormulaId()).ifPresent(f -> {
            f.setEstadoDespacho(nuevo);
            formulaRepository.save(f);
        });
        log(d, anterior, nuevo, motivo);
    }

    private void log(Despacho d, String anterior, String nuevo, String motivo) {
        DespachoEstadoLog l = new DespachoEstadoLog();
        l.setDespachoId(d.getId());
        l.setEstadoAnterior(anterior);
        l.setEstadoNuevo(nuevo);
        try {
            l.setUsuarioId(usuarioActual.id());
        } catch (RuntimeException e) {
            l.setUsuarioId(null);
        }
        l.setFecha(LocalDateTime.now());
        l.setMotivo(motivo == null || motivo.length() <= 500 ? motivo : motivo.substring(0, 500));
        logRepository.save(l);
    }

    private Resumen resumen(Despacho d) {
        FormulaMedica f = formulaRepository.findById(d.getFormulaId()).orElseThrow();
        Cita c = f.getCita();
        return new Resumen(d.getId(), f.getId(), d.getEstado(), d.getFechaCreacion(),
                c.getCliente().getNombres() + " " + c.getCliente().getApellidos(), c.getMascota().getNombreMascota(),
                c.getMedico().getNombres() + " " + c.getMedico().getApellidos(), d.getDetalles().size());
    }

    private Detalle detalle(Despacho d) {
        FormulaMedica f = formulaRepository.findById(d.getFormulaId()).orElseThrow();
        Resumen r = resumen(d);
        List<Linea> lineas = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        for (DespachoDetalle dd : d.getDetalles()) {
            Medicamento m = medicamentoRepository.findById(dd.getMedicamentoId()).orElseThrow();
            List<LoteInfo> lotes = loteRepository.findAllByMedicamentoIdOrderByFechaVencimientoAsc(m.getId()).stream()
                    .filter(l -> l.getCantidadDisponible() > 0)
                    .map(l -> new LoteInfo(l.getId(), l.getNumeroLote(), l.getCantidadDisponible(), l.getFechaVencimiento(),
                            l.getFechaVencimiento() == null ? "VIGENTE"
                                    : l.getFechaVencimiento().isBefore(hoy) ? "VENCIDO"
                                    : l.getFechaVencimiento().isBefore(hoy.plusDays(DIAS_ALERTA + 1)) ? "PROXIMO_A_VENCER" : "VIGENTE"))
                    .toList();
            lineas.add(new Linea(dd.getId(), m.getId(), m.getCodigo(), m.getNombre(), dd.getCantidadSolicitada(),
                    dd.getCantidadEntregada(), dd.getCantidadSolicitada() - dd.getCantidadEntregada(),
                    loteRepository.stockVigente(m.getId(), hoy).intValue(), lotes));
        }
        return new Detalle(d.getId(), f.getId(), d.getEstado(), d.getFechaCreacion(), r.propietario(), r.mascota(),
                r.medico(), f.getIndicaciones(), lineas);
    }
}
