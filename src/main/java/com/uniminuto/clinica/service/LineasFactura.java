package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Despacho;
import com.uniminuto.clinica.entity.DespachoDetalle;
import com.uniminuto.clinica.entity.FacturaDetalle;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.Tarifa;
import com.uniminuto.clinica.repository.DespachoRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Construye las líneas de factura/prefactura (servicio + medicamentos) congelando precios e IVA.
 * Una factura incluye solo medicamentos efectivamente despachados; la prefactura, los formulados.
 */
@Component
public class LineasFactura {

    @Autowired
    private FormulaMedicaRepository formulaRepository;

    @Autowired
    private DespachoRepository despachoRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    /**
     * Línea del servicio (consulta) con el valor y el IVA vigentes de la tarifa.
     *
     * @param t tarifa.
     * @return línea de servicio.
     */
    public FacturaDetalle servicio(Tarifa t) {
        return linea("SERVICIO", t.getServicio(), t.getId(), null, 1, t.getValor(), t.getIvaPorcentaje());
    }

    /**
     * Líneas de medicamentos de las fórmulas de una cita.
     *
     * @param citaId        cita.
     * @param soloEntregado {@code true}: cantidad entregada (factura); {@code false}: cantidad prescrita (prefactura).
     * @return líneas de medicamentos con cantidad mayor que cero.
     */
    public List<FacturaDetalle> medicamentos(Long citaId, boolean soloEntregado) {
        List<FacturaDetalle> lineas = new ArrayList<>();
        for (FormulaMedica f : formulaRepository.findAllByCita_IdOrderByIdAsc(citaId)) {
            Despacho d = despachoRepository.findByFormulaId(f.getId()).orElse(null);
            if (d == null) {
                continue;
            }
            for (DespachoDetalle dd : d.getDetalles()) {
                int cantidad = soloEntregado ? dd.getCantidadEntregada() : dd.getCantidadSolicitada();
                if (cantidad <= 0) {
                    continue;
                }
                Medicamento m = medicamentoRepository.findById(dd.getMedicamentoId()).orElseThrow();
                lineas.add(linea("MEDICAMENTO", m.getNombre(), null, m.getId(), cantidad, m.getPrecioVenta(), m.getIvaPorcentaje()));
            }
        }
        return lineas;
    }

    /**
     * Construye una línea calculando subtotal e IVA.
     *
     * @param tipo SERVICIO o MEDICAMENTO. @param desc descripción. @param tarifaId tarifa. @param medId medicamento.
     * @param cantidad unidades. @param precio precio unitario. @param iva IVA en porcentaje.
     * @return línea.
     */
    public FacturaDetalle linea(String tipo, String desc, Long tarifaId, Long medId, int cantidad, BigDecimal precio, BigDecimal iva) {
        FacturaDetalle l = new FacturaDetalle();
        l.setTipo(tipo);
        l.setDescripcion(desc);
        l.setTarifaId(tarifaId);
        l.setMedicamentoId(medId);
        l.setCantidad(cantidad);
        l.setPrecioUnitario(precio);
        l.setIvaPorcentaje(iva);
        BigDecimal sub = precio.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
        l.setSubtotal(sub);
        l.setIvaValor(sub.multiply(iva).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        return l;
    }

    /**
     * @param lineas líneas.
     * @return suma de subtotales.
     */
    public BigDecimal subtotal(List<FacturaDetalle> lineas) {
        return lineas.stream().map(FacturaDetalle::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * @param lineas líneas.
     * @return suma del IVA.
     */
    public BigDecimal impuestos(List<FacturaDetalle> lineas) {
        return lineas.stream().map(FacturaDetalle::getIvaValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
