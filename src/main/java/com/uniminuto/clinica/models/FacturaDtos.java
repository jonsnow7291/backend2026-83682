package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.Factura;
import com.uniminuto.clinica.entity.Pago;

import java.math.BigDecimal;
import java.util.List;

/**
 * Objetos de transferencia de facturación y pagos.
 */
public final class FacturaDtos {

    private FacturaDtos() {
    }

    /** Solicitud para generar la factura de una cita atendida. */
    public record GenerarRq(Long citaId, BigDecimal descuento) {
    }

    /** Solicitud para registrar un pago. */
    public record PagoRq(Long facturaId, BigDecimal valor, String metodo, String referencia) {
    }

    /** Factura con sus pagos y datos descriptivos. */
    public record FacturaRs(Factura factura, String propietario, String mascota, String veterinario,
                            String especialidad, List<Pago> pagos, BigDecimal totalPagado, BigDecimal saldo) {
    }
}
