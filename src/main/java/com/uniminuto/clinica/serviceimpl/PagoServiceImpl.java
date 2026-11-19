package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Factura;
import com.uniminuto.clinica.entity.Pago;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.FacturaDtos.FacturaRs;
import com.uniminuto.clinica.models.FacturaDtos.PagoRq;
import com.uniminuto.clinica.repository.FacturaRepository;
import com.uniminuto.clinica.repository.PagoRepository;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.service.FacturaService;
import com.uniminuto.clinica.service.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación de pagos: siempre ligados a una factura, nunca superan el saldo.
 */
@Service
public class PagoServiceImpl implements PagoService {

    private static final List<String> METODOS = List.of("EFECTIVO", "TARJETA", "TRANSFERENCIA");

    @Autowired private PagoRepository pagoRepository;
    @Autowired private FacturaRepository facturaRepository;
    @Autowired private FacturaService facturaService;
    @Autowired private UsuarioActual usuarioActual;

    /** {@inheritDoc} */
    @Override
    @Transactional
    public FacturaRs registrar(PagoRq rq) {
        if (rq == null || rq.facturaId() == null) {
            throw new BadRequestException("Todo pago debe estar asociado a una factura");
        }
        Factura f = facturaRepository.bloquear(rq.facturaId())
                .orElseThrow(() -> new NotFoundException("La factura " + rq.facturaId() + " no existe"));
        if ("ANULADA".equals(f.getEstadoPago())) {
            throw new ConflictException("No se puede pagar una factura anulada");
        }
        if ("PAGADA".equals(f.getEstadoPago())) {
            throw new ConflictException("La factura ya está pagada");
        }
        if (rq.valor() == null || rq.valor().signum() <= 0) {
            throw new BadRequestException("El valor del pago debe ser mayor que cero");
        }
        String metodo = rq.metodo() == null ? "" : rq.metodo().trim().toUpperCase();
        if (!METODOS.contains(metodo)) {
            throw new BadRequestException("El método de pago debe ser " + METODOS);
        }
        BigDecimal saldo = f.getTotal().subtract(pagoRepository.totalPagado(f.getId()));
        if (rq.valor().compareTo(saldo) > 0) {
            throw new BadRequestException("El pago (" + rq.valor() + ") supera el saldo pendiente (" + saldo + ")");
        }
        Pago p = new Pago();
        p.setFacturaId(f.getId());
        p.setValor(rq.valor());
        p.setMetodo(metodo);
        p.setReferencia(rq.referencia());
        p.setFecha(LocalDateTime.now());
        p.setUsuarioId(usuarioActual.id());
        pagoRepository.save(p);
        f.setEstadoPago(rq.valor().compareTo(saldo) == 0 ? "PAGADA" : "PARCIAL");
        facturaRepository.save(f);
        return facturaService.armar(f.getId());
    }

    /** {@inheritDoc} */
    @Override
    public List<Pago> listar(Long facturaId) {
        return pagoRepository.findAllByFacturaIdOrderByFecha(facturaId);
    }
}
