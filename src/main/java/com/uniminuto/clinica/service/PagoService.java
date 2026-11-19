package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Pago;
import com.uniminuto.clinica.models.FacturaDtos;

import java.util.List;

/**
 * Pagos de facturas (parciales o totales).
 */
public interface PagoService {

    /**
     * Registra un pago y recalcula el estado de pago de la factura.
     *
     * @param rq pago.
     * @return factura actualizada.
     */
    FacturaDtos.FacturaRs registrar(FacturaDtos.PagoRq rq);

    /**
     * @param facturaId factura.
     * @return pagos de la factura.
     */
    List<Pago> listar(Long facturaId);
}
