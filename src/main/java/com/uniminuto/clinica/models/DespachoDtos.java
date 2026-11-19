package com.uniminuto.clinica.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Objetos de transferencia del módulo de despacho.
 */
public final class DespachoDtos {

    private DespachoDtos() {
    }

    /** Resumen de un despacho para listados y búsquedas. */
    public record Resumen(Long id, Long formulaId, String estado, LocalDateTime fecha, String propietario,
                          String mascota, String medico, int items) {
    }

    /** Lote disponible de un medicamento con su vigencia (VIGENTE, PROXIMO_A_VENCER o VENCIDO). */
    public record LoteInfo(Long loteId, String numeroLote, int disponible, LocalDate vence, String vigencia) {
    }

    /** Línea de un despacho con existencias y lotes. */
    public record Linea(Long detalleId, Long medicamentoId, String codigo, String medicamento, int solicitada,
                        int entregada, int pendiente, int stockVigente, List<LoteInfo> lotes) {
    }

    /** Detalle de la fórmula a despachar. */
    public record Detalle(Long despachoId, Long formulaId, String estado, LocalDateTime fecha, String propietario,
                          String mascota, String medico, String indicaciones, List<Linea> lineas) {
    }

    /** Asignación sugerida de un lote (FEFO) para una línea. */
    public record Asignacion(Long detalleId, String medicamento, Long loteId, String numeroLote, int cantidad,
                             LocalDate vence) {
    }

    /** Sugerencia FEFO completa: asignaciones y faltantes por línea. */
    public record Sugerencia(Long despachoId, List<Asignacion> asignaciones, List<String> faltantes) {
    }

    /** Cantidad a entregar en una línea al confirmar. */
    public record LineaEntrega(Long detalleId, Integer cantidad) {
    }

    /** Solicitud de confirmación; sin líneas se entrega todo lo pendiente que haya disponible. */
    public record ConfirmarRq(List<LineaEntrega> lineas, String observaciones) {
    }

    /** Autorización de un ADMIN para cancelar o reversar un despacho. */
    public record AutorizacionRq(String motivo, String autorizadorUsername, String autorizadorPassword) {
    }

    /** Movimiento de un medicamento ligado a un despacho (historial por medicamento). */
    public record MovimientoDespacho(Long movimientoId, Long despachoId, Long formulaId, String tipo, int cantidad,
                                     String lote, LocalDateTime fecha, Long usuarioId, String motivo) {
    }
}
