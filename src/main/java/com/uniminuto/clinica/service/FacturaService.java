package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.FacturaDtos;

import java.util.List;

/**
 * Facturación de citas atendidas. Las facturas nunca se borran: se anulan conservando el histórico.
 */
public interface FacturaService {

    /**
     * Genera la factura de una cita atendida con el servicio y los medicamentos efectivamente despachados.
     *
     * @param rq cita y descuento opcional.
     * @return factura generada.
     */
    FacturaDtos.FacturaRs generar(FacturaDtos.GenerarRq rq);

    /**
     * @param id factura.
     * @return factura con pagos y saldo.
     */
    FacturaDtos.FacturaRs consultar(Long id);

    /**
     * @param clienteId filtro por propietario (opcional).
     * @param mascotaId filtro por mascota (opcional).
     * @return historial de facturación.
     */
    List<FacturaDtos.FacturaRs> listar(Long clienteId, Integer mascotaId);

    /**
     * Anula una factura (sin pagos registrados) conservando el histórico.
     *
     * @param id     factura.
     * @param motivo motivo obligatorio.
     * @return factura anulada.
     */
    FacturaDtos.FacturaRs anular(Long id, String motivo);

    /**
     * @param id factura.
     * @return PDF con los campos de la factura.
     */
    byte[] pdf(Long id);

    /**
     * Construye la respuesta de una factura.
     *
     * @param id factura.
     * @return factura con pagos y saldo.
     */
    FacturaDtos.FacturaRs armar(Long id);
}
