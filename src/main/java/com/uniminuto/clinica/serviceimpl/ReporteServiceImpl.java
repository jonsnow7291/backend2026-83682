package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ReporteData;
import com.uniminuto.clinica.service.ReporteService;
import com.uniminuto.clinica.util.ExcelUtil;
import com.uniminuto.clinica.util.PdfBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.ResultSetMetaData;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementación de reportes con SQL directo sobre las mismas tablas que usan facturación, pagos
 * e inventario, de modo que los totales cuadran entre reportes: los ingresos facturados excluyen
 * facturas ANULADAS y los pagos se reportan aparte.
 */
@Service
public class ReporteServiceImpl implements ReporteService {

    private static final Map<String, String> CATALOGO = new LinkedHashMap<>();

    static {
        CATALOGO.put("ingresos-periodo", "Ingresos (pagos recibidos) por período");
        CATALOGO.put("ingresos-consultas", "Ingresos facturados por consultas/servicios");
        CATALOGO.put("ingresos-medicamentos", "Ingresos facturados por venta de medicamentos");
        CATALOGO.put("ingresos-especialidad", "Ingresos facturados por especialidad");
        CATALOGO.put("ingresos-veterinario", "Ingresos facturados por veterinario");
        CATALOGO.put("facturas-resumen", "Facturas por estado, impuestos y descuentos");
        CATALOGO.put("resumen-financiero", "Resumen financiero por día/mes/año (agrupacion=DIA|MES|ANIO)");
        CATALOGO.put("inventario-existencias", "Existencias, valor a costo y valor potencial de venta");
        CATALOGO.put("inventario-agotados", "Productos agotados");
        CATALOGO.put("inventario-stock-minimo", "Productos en stock mínimo o por debajo");
        CATALOGO.put("inventario-proximos-vencer", "Lotes próximos a vencer (30 días)");
        CATALOGO.put("inventario-vencidos", "Lotes vencidos con existencias");
        CATALOGO.put("inventario-movimientos", "Entradas y salidas de inventario del período");
        CATALOGO.put("inventario-historial-medicamento", "Historial de un medicamento (medicamentoId)");
        CATALOGO.put("citas-periodo", "Citas por período");
        CATALOGO.put("citas-estado", "Citas por estado (atendidas, canceladas, pendientes...)");
        CATALOGO.put("consultas-veterinario", "Consultas atendidas por veterinario");
        CATALOGO.put("consultas-especialidad", "Consultas atendidas por especialidad");
        CATALOGO.put("mascotas-atendidas", "Mascotas atendidas en el período");
    }

    @Autowired
    private JdbcTemplate jdbc;

    /** {@inheritDoc} */
    @Override
    public List<String> catalogo() {
        return CATALOGO.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).toList();
    }

    /** {@inheritDoc} */
    @Override
    public ReporteData generar(String tipo, LocalDate desde, LocalDate hasta, Long medicoId, Long esp,
                               Long medicamentoId, String agrupacion) {
        if (tipo == null || !CATALOGO.containsKey(tipo)) {
            throw new BadRequestException("Reporte desconocido. Disponibles: " + CATALOGO.keySet());
        }
        LocalDate d = desde != null ? desde : LocalDate.now().withDayOfMonth(1);
        LocalDate h = hasta != null ? hasta : LocalDate.now();
        if (h.isBefore(d)) {
            throw new BadRequestException("La fecha 'hasta' no puede ser anterior a 'desde'");
        }
        LocalDateTime ini = d.atStartOfDay();
        LocalDateTime fin = h.plusDays(1).atStartOfDay();
        String titulo = CATALOGO.get(tipo);
        String fm = medicoId == null ? "" : " AND f.medico_id = " + medicoId;
        String fe = esp == null ? "" : " AND f.especializacion_id = " + esp;
        String FAC = " FROM factura f WHERE f.estado_pago <> 'ANULADA' AND f.fecha >= ? AND f.fecha < ?" + fm + fe;
        return switch (tipo) {
            case "ingresos-periodo" -> r(tipo, titulo, d, h, List.of("Fecha", "Factura", "Método", "Valor"),
                    "SELECT p.fecha, f.numero, p.metodo, p.valor FROM pago p JOIN factura f ON f.id = p.factura_id "
                            + "WHERE f.estado_pago <> 'ANULADA' AND p.fecha >= ? AND p.fecha < ?" + fm + fe + " ORDER BY p.fecha",
                    List.of(3), "Total ingresos", ini, fin);
            case "ingresos-consultas" -> detalleLineas(tipo, titulo, d, h, "SERVICIO", fm + fe, ini, fin);
            case "ingresos-medicamentos" -> detalleLineas(tipo, titulo, d, h, "MEDICAMENTO", fm + fe, ini, fin);
            case "ingresos-especialidad" -> r(tipo, titulo, d, h, List.of("Especialidad", "Facturas", "Total facturado"),
                    "SELECT COALESCE(e.nombre,'-'), COUNT(*), SUM(f.total) FROM factura f LEFT JOIN especializacion e ON e.id = f.especializacion_id "
                            + "WHERE f.estado_pago <> 'ANULADA' AND f.fecha >= ? AND f.fecha < ?" + fm + fe + " GROUP BY e.nombre ORDER BY 3 DESC",
                    List.of(1, 2), "Total", ini, fin);
            case "ingresos-veterinario" -> r(tipo, titulo, d, h, List.of("Veterinario", "Facturas", "Total facturado"),
                    "SELECT CONCAT(m.nombres,' ',m.apellidos), COUNT(*), SUM(f.total) FROM factura f JOIN medico m ON m.id = f.medico_id "
                            + "WHERE f.estado_pago <> 'ANULADA' AND f.fecha >= ? AND f.fecha < ?" + fm + fe + " GROUP BY m.id, m.nombres, m.apellidos ORDER BY 3 DESC",
                    List.of(1, 2), "Total", ini, fin);
            case "facturas-resumen" -> facturasResumen(tipo, titulo, d, h, fm + fe, ini, fin);
            case "resumen-financiero" -> resumenFinanciero(tipo, titulo, d, h, agrupacion, fm + fe, ini, fin);
            case "inventario-existencias" -> existencias(tipo, titulo);
            case "inventario-agotados" -> r(tipo, titulo, null, null, List.of("Código", "Producto", "Tipo", "Stock", "Stock mínimo"),
                    "SELECT codigo, nombre, tipo, stock_actual, stock_minimo FROM medicamento WHERE activo = TRUE AND stock_actual <= 0 ORDER BY nombre",
                    List.of(), null);
            case "inventario-stock-minimo" -> r(tipo, titulo, null, null, List.of("Código", "Producto", "Tipo", "Stock", "Stock mínimo"),
                    "SELECT codigo, nombre, tipo, stock_actual, stock_minimo FROM medicamento WHERE activo = TRUE AND stock_actual > 0 AND stock_actual <= stock_minimo ORDER BY nombre",
                    List.of(), null);
            case "inventario-proximos-vencer" -> r(tipo, titulo, null, null, List.of("Código", "Producto", "Lote", "Disponible", "Vence"),
                    "SELECT m.codigo, m.nombre, l.numero_lote, l.cantidad_disponible, l.fecha_vencimiento FROM lote_medicamento l JOIN medicamento m ON m.id = l.medicamento_id "
                            + "WHERE l.cantidad_disponible > 0 AND l.fecha_vencimiento BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY) ORDER BY l.fecha_vencimiento",
                    List.of(3), "Unidades");
            case "inventario-vencidos" -> r(tipo, titulo, null, null, List.of("Código", "Producto", "Lote", "Disponible", "Venció"),
                    "SELECT m.codigo, m.nombre, l.numero_lote, l.cantidad_disponible, l.fecha_vencimiento FROM lote_medicamento l JOIN medicamento m ON m.id = l.medicamento_id "
                            + "WHERE l.cantidad_disponible > 0 AND l.fecha_vencimiento < CURDATE() ORDER BY l.fecha_vencimiento",
                    List.of(3), "Unidades");
            case "inventario-movimientos" -> r(tipo, titulo, d, h, List.of("Fecha", "Producto", "Lote", "Tipo", "Cantidad", "Despacho", "Fórmula", "Usuario"),
                    "SELECT mv.fecha, m.nombre, l.numero_lote, mv.tipo, mv.cantidad, mv.despacho_id, mv.formula_medica_id, mv.usuario_id FROM movimiento_inventario mv "
                            + "JOIN medicamento m ON m.id = mv.medicamento_id LEFT JOIN lote_medicamento l ON l.id = mv.lote_id WHERE mv.fecha >= ? AND mv.fecha < ? ORDER BY mv.fecha",
                    List.of(4), "Unidades movidas", ini, fin);
            case "inventario-historial-medicamento" -> {
                if (medicamentoId == null) {
                    throw new BadRequestException("El parámetro medicamentoId es obligatorio para este reporte");
                }
                yield r(tipo, titulo, null, null, List.of("Fecha", "Lote", "Tipo", "Cantidad", "Despacho", "Fórmula", "Usuario", "Motivo"),
                        "SELECT mv.fecha, l.numero_lote, mv.tipo, mv.cantidad, mv.despacho_id, mv.formula_medica_id, mv.usuario_id, mv.motivo FROM movimiento_inventario mv "
                                + "LEFT JOIN lote_medicamento l ON l.id = mv.lote_id WHERE mv.medicamento_id = ? ORDER BY mv.fecha",
                        List.of(3), "Unidades movidas", medicamentoId);
            }
            case "citas-periodo" -> r(tipo, titulo, d, h, List.of("Id", "Inicio", "Estado", "Mascota", "Propietario", "Veterinario", "Motivo"),
                    "SELECT c.id, c.fecha_hora, c.estado, ma.nombre_mascota, CONCAT(cl.nombres,' ',cl.apellidos), CONCAT(m.nombres,' ',m.apellidos), c.motivo FROM cita c "
                            + "JOIN mascota ma ON ma.mascota_id = c.mascota_id JOIN cliente cl ON cl.cliente_id = c.cliente_id JOIN medico m ON m.id = c.medico_id "
                            + "WHERE c.fecha_hora >= ? AND c.fecha_hora < ?" + citaFiltro(medicoId, esp) + " ORDER BY c.fecha_hora",
                    List.of(), null, ini, fin);
            case "citas-estado" -> r(tipo, titulo, d, h, List.of("Estado", "Citas"),
                    "SELECT c.estado, COUNT(*) FROM cita c WHERE c.fecha_hora >= ? AND c.fecha_hora < ?" + citaFiltro(medicoId, esp) + " GROUP BY c.estado ORDER BY 2 DESC",
                    List.of(1), "Total citas", ini, fin);
            case "consultas-veterinario" -> r(tipo, titulo, d, h, List.of("Veterinario", "Consultas atendidas"),
                    "SELECT CONCAT(m.nombres,' ',m.apellidos), COUNT(*) FROM cita c JOIN medico m ON m.id = c.medico_id WHERE c.estado IN ('ATENDIDA','FINALIZADA') "
                            + "AND c.fecha_hora >= ? AND c.fecha_hora < ?" + citaFiltro(medicoId, esp) + " GROUP BY m.id, m.nombres, m.apellidos ORDER BY 2 DESC",
                    List.of(1), "Total consultas", ini, fin);
            case "consultas-especialidad" -> r(tipo, titulo, d, h, List.of("Especialidad", "Consultas atendidas"),
                    "SELECT COALESCE(e.nombre,'-'), COUNT(*) FROM cita c LEFT JOIN especializacion e ON e.id = c.especializacion_id WHERE c.estado IN ('ATENDIDA','FINALIZADA') "
                            + "AND c.fecha_hora >= ? AND c.fecha_hora < ?" + citaFiltro(medicoId, esp) + " GROUP BY e.nombre ORDER BY 2 DESC",
                    List.of(1), "Total consultas", ini, fin);
            default -> r(tipo, titulo, d, h, List.of("Mascota", "Propietario", "Atenciones", "Última atención"),
                    "SELECT ma.nombre_mascota, CONCAT(cl.nombres,' ',cl.apellidos), COUNT(*), MAX(c.fecha_hora) FROM cita c JOIN mascota ma ON ma.mascota_id = c.mascota_id "
                            + "JOIN cliente cl ON cl.cliente_id = c.cliente_id WHERE c.estado IN ('ATENDIDA','FINALIZADA') AND c.fecha_hora >= ? AND c.fecha_hora < ?"
                            + citaFiltro(medicoId, esp) + " GROUP BY ma.mascota_id, ma.nombre_mascota, cl.nombres, cl.apellidos ORDER BY 3 DESC",
                    List.of(2), "Total atenciones", ini, fin);
        };
    }

    private String citaFiltro(Long medicoId, Long esp) {
        return (medicoId == null ? "" : " AND c.medico_id = " + medicoId) + (esp == null ? "" : " AND c.especializacion_id = " + esp);
    }

    /** Ejecuta la consulta y arma el reporte sumando las columnas indicadas. */
    private ReporteData r(String codigo, String titulo, LocalDate d, LocalDate h, List<String> cols, String sql,
                          List<Integer> sumar, String etiquetaTotal, Object... args) {
        List<List<Object>> filas = jdbc.query(sql, (rs, n) -> {
            ResultSetMetaData md = rs.getMetaData();
            List<Object> fila = new ArrayList<>();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                Object v = rs.getObject(i);
                fila.add(v == null ? "" : (v instanceof java.sql.Timestamp t ? t.toLocalDateTime().withNano(0).toString().replace('T', ' ') : v));
            }
            return fila;
        }, args);
        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("Registros", filas.size());
        for (int idx : sumar) {
            BigDecimal suma = BigDecimal.ZERO;
            for (List<Object> f : filas) {
                if (f.get(idx) instanceof Number n) {
                    suma = suma.add(new BigDecimal(n.toString()));
                }
            }
            totales.put(etiquetaTotal != null && sumar.size() == 1 ? etiquetaTotal : cols.get(idx), suma);
        }
        return new ReporteData(codigo, titulo, d, h, LocalDateTime.now().withNano(0), cols, filas, totales);
    }

    /** Variante sin argumentos de fecha ni etiqueta. */
    private ReporteData r(String codigo, String titulo, LocalDate d, LocalDate h, List<String> cols, String sql,
                          List<Integer> sumar, String etiqueta) {
        return r(codigo, titulo, d, h, cols, sql, sumar, etiqueta, new Object[0]);
    }

    private ReporteData detalleLineas(String tipo, String titulo, LocalDate d, LocalDate h, String lineaTipo, String filtros,
                                      LocalDateTime ini, LocalDateTime fin) {
        return r(tipo, titulo, d, h, List.of("Factura", "Fecha", "Descripción", "Cantidad", "Total línea"),
                "SELECT f.numero, f.fecha, fd.descripcion, fd.cantidad, fd.subtotal + fd.iva_valor FROM factura_detalle fd JOIN factura f ON f.id = fd.factura_id "
                        + "WHERE f.estado_pago <> 'ANULADA' AND fd.tipo = '" + lineaTipo + "' AND f.fecha >= ? AND f.fecha < ?" + filtros + " ORDER BY f.fecha",
                List.of(4), "Total", ini, fin);
    }

    private ReporteData facturasResumen(String tipo, String titulo, LocalDate d, LocalDate h, String filtros, LocalDateTime ini, LocalDateTime fin) {
        ReporteData base = r(tipo, titulo, d, h, List.of("Estado de pago", "Facturas", "Subtotal", "Impuestos", "Descuentos", "Total"),
                "SELECT f.estado_pago, COUNT(*), SUM(f.subtotal), SUM(f.impuestos), SUM(f.descuento), SUM(f.total) FROM factura f WHERE f.fecha >= ? AND f.fecha < ?"
                        + filtros + " GROUP BY f.estado_pago ORDER BY 1",
                List.of(1, 2, 3, 4, 5), null, ini, fin);
        return base;
    }

    private ReporteData resumenFinanciero(String tipo, String titulo, LocalDate d, LocalDate h, String agrupacion, String filtros,
                                          LocalDateTime ini, LocalDateTime fin) {
        String a = agrupacion == null ? "DIA" : agrupacion.toUpperCase();
        String fmt = switch (a) {
            case "DIA" -> "%Y-%m-%d";
            case "MES" -> "%Y-%m";
            case "ANIO" -> "%Y";
            default -> throw new BadRequestException("agrupacion debe ser DIA, MES o ANIO");
        };
        return r(tipo, titulo + " (" + a + ")", d, h, List.of("Período", "Facturas", "Facturado", "Impuestos", "Descuentos"),
                "SELECT DATE_FORMAT(f.fecha, '" + fmt + "'), COUNT(*), SUM(f.total), SUM(f.impuestos), SUM(f.descuento) FROM factura f "
                        + "WHERE f.estado_pago <> 'ANULADA' AND f.fecha >= ? AND f.fecha < ?" + filtros + " GROUP BY 1 ORDER BY 1",
                List.of(1, 2, 3, 4), null, ini, fin);
    }

    private ReporteData existencias(String tipo, String titulo) {
        ReporteData base = r(tipo, titulo, null, null,
                List.of("Código", "Producto", "Tipo", "Existencias vigentes", "Valor a costo", "Valor potencial de venta"),
                "SELECT m.codigo, m.nombre, m.tipo, COALESCE(SUM(l.cantidad_disponible),0), COALESCE(SUM(l.cantidad_disponible * l.precio_compra),0), "
                        + "COALESCE(SUM(l.cantidad_disponible * m.precio_venta),0) FROM medicamento m LEFT JOIN lote_medicamento l ON l.medicamento_id = m.id AND l.fecha_vencimiento >= CURDATE() "
                        + "WHERE m.activo = TRUE GROUP BY m.id, m.codigo, m.nombre, m.tipo ORDER BY m.nombre",
                List.of(3, 4, 5), null);
        return base;
    }

    /** {@inheritDoc} */
    @Override
    public byte[] pdf(ReporteData r) {
        PdfBuilder b = new PdfBuilder(r.titulo(), r.columnas().size() > 4);
        if (r.desde() != null) {
            b.campo("Período", r.desde() + " a " + r.hasta());
        }
        b.tabla(r.columnas(), r.filas()).seccion("Totales");
        r.totales().forEach(b::campo);
        return b.build();
    }

    /** {@inheritDoc} */
    @Override
    public byte[] xlsx(ReporteData r) {
        String t = r.desde() == null ? r.titulo() : r.titulo() + " (" + r.desde() + " a " + r.hasta() + ")";
        return ExcelUtil.generar(r.codigo(), t, r.columnas(), r.filas(), r.totales());
    }
}
