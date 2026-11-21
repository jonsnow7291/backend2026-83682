package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.LoteMedicamento;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.LoteRq;
import com.uniminuto.clinica.repository.LoteRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.repository.MovimientoRepository;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del inventario. Regla de stock: {@code medicamento.stock_actual} se sincroniza
 * con la suma de lotes vigentes dentro de la misma transacción de cada movimiento.
 * Reglas: stock nunca negativo, vencimiento posterior a la compra, no se descuenta de lotes vencidos.
 */
@Service
public class InventarioServiceImpl implements InventarioService {

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    @Autowired
    private UsuarioActual usuarioActual;

    /** {@inheritDoc} */
    @Override
    @Transactional
    public LoteMedicamento registrarEntrada(LoteRq rq) {
        validarLote(rq);
        Medicamento m = medicamentoRepository.findById(rq.getMedicamentoId())
                .orElseThrow(() -> new NotFoundException("El producto con id " + rq.getMedicamentoId() + " no existe"));
        if ("MEDICAMENTO".equals(m.getTipo()) && rq.getFechaVencimiento() == null) {
            throw new BadRequestException("La fecha de vencimiento es obligatoria para los medicamentos");
        }
        if (!Boolean.TRUE.equals(m.getActivo())) {
            throw new BadRequestException("El producto está inactivo y no admite entradas");
        }
        String numero = rq.getNumeroLote().trim();
        if (loteRepository.findByMedicamentoIdAndNumeroLote(m.getId(), numero).isPresent()) {
            throw new BadRequestException("Ya existe el lote '" + numero + "' para este producto");
        }
        LoteMedicamento l = new LoteMedicamento();
        l.setMedicamentoId(m.getId());
        l.setNumeroLote(numero);
        l.setCantidadInicial(rq.getCantidad());
        l.setCantidadDisponible(rq.getCantidad());
        l.setPrecioCompra(rq.getPrecioCompra() == null ? m.getPrecioCompra() : rq.getPrecioCompra());
        l.setFechaCompra(rq.getFechaCompra());
        l.setFechaVencimiento(rq.getFechaVencimiento());
        l.setFechaCreacion(LocalDateTime.now());
        l = loteRepository.save(l);
        movimiento(m.getId(), l.getId(), "ENTRADA", rq.getCantidad(), null, null, "Entrada de lote " + numero);
        sincronizarStock(m.getId());
        return l;
    }

    /**
     * Valida los datos de un lote.
     *
     * @param rq datos recibidos.
     */
    private void validarLote(LoteRq rq) {
        if (rq == null || rq.getMedicamentoId() == null) {
            throw new BadRequestException("El producto es obligatorio");
        }
        if (rq.getNumeroLote() == null || rq.getNumeroLote().isBlank()) {
            throw new BadRequestException("El número de lote es obligatorio");
        }
        if (rq.getCantidad() == null || rq.getCantidad() <= 0) {
            throw new BadRequestException("La cantidad debe ser mayor que cero");
        }
        if (rq.getPrecioCompra() != null && rq.getPrecioCompra().signum() < 0) {
            throw new BadRequestException("El precio de compra no puede ser negativo");
        }
        if (rq.getFechaCompra() == null) {
            throw new BadRequestException("La fecha de compra es obligatoria");
        }
        // El vencimiento es opcional solo para utensilios (se exige para MEDICAMENTO en registrarEntrada).
        if (rq.getFechaVencimiento() != null && !rq.getFechaVencimiento().isAfter(rq.getFechaCompra())) {
            throw new BadRequestException("La fecha de vencimiento debe ser posterior a la fecha de compra");
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<LoteMedicamento> listarLotes(Long medicamentoId) {
        if (medicamentoId == null) {
            throw new BadRequestException("El parámetro medicamentoId es obligatorio");
        }
        return loteRepository.findAllByMedicamentoIdOrderByFechaVencimientoAsc(medicamentoId);
    }

    /** {@inheritDoc} */
    @Override
    public List<MovimientoInventario> listarMovimientos(Long medicamentoId) {
        return medicamentoId == null
                ? movimientoRepository.findAllByOrderByFechaDescIdDesc()
                : movimientoRepository.findAllByMedicamentoIdOrderByFechaDescIdDesc(medicamentoId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void registrarSalida(Long loteId, int cantidad, Long formulaId, Long despachoId, String motivo) {
        if (cantidad <= 0) {
            throw new BadRequestException("La cantidad a descontar debe ser mayor que cero");
        }
        LoteMedicamento l = loteRepository.bloquear(loteId)
                .orElseThrow(() -> new NotFoundException("El lote " + loteId + " no existe"));
        if (l.getFechaVencimiento() != null && l.getFechaVencimiento().isBefore(LocalDate.now())) {
            throw new BadRequestException("El lote " + l.getNumeroLote() + " está vencido y no puede despacharse");
        }
        if (l.getCantidadDisponible() < cantidad) {
            throw new BadRequestException("El lote " + l.getNumeroLote() + " solo tiene "
                    + l.getCantidadDisponible() + " unidades disponibles");
        }
        l.setCantidadDisponible(l.getCantidadDisponible() - cantidad);
        loteRepository.save(l);
        movimiento(l.getMedicamentoId(), l.getId(), "SALIDA", cantidad, formulaId, despachoId, motivo);
        sincronizarStock(l.getMedicamentoId());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void registrarReversion(Long loteId, int cantidad, Long formulaId, Long despachoId, String motivo) {
        LoteMedicamento l = loteRepository.bloquear(loteId)
                .orElseThrow(() -> new NotFoundException("El lote " + loteId + " no existe"));
        l.setCantidadDisponible(l.getCantidadDisponible() + cantidad);
        loteRepository.save(l);
        movimiento(l.getMedicamentoId(), l.getId(), "ENTRADA", cantidad, formulaId, despachoId, motivo);
        sincronizarStock(l.getMedicamentoId());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void sincronizarStock(Long medicamentoId) {
        Medicamento m = medicamentoRepository.findById(medicamentoId)
                .orElseThrow(() -> new NotFoundException("El producto " + medicamentoId + " no existe"));
        m.setStockActual(loteRepository.stockVigente(medicamentoId, LocalDate.now()).intValue());
        m.setFechaModificacionRegistro(LocalDateTime.now());
        medicamentoRepository.save(m);
    }

    private void movimiento(Long medId, Long loteId, String tipo, int cantidad, Long formulaId, Long despachoId, String motivo) {
        MovimientoInventario mv = new MovimientoInventario();
        mv.setMedicamentoId(medId);
        mv.setLoteId(loteId);
        mv.setTipo(tipo);
        mv.setCantidad(cantidad);
        mv.setFecha(LocalDateTime.now());
        mv.setUsuarioId(usuarioActualId());
        mv.setFormulaMedicaId(formulaId);
        mv.setDespachoId(despachoId);
        mv.setMotivo(motivo);
        movimientoRepository.save(mv);
    }

    /**
     * @return id del usuario autenticado, o {@code null} fuera de una solicitud autenticada.
     */
    private Long usuarioActualId() {
        try {
            return usuarioActual.id();
        } catch (RuntimeException e) {
            return null;
        }
    }
}
