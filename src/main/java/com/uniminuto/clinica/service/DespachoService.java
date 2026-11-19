package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Despacho;
import com.uniminuto.clinica.entity.DespachoEstadoLog;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.models.DespachoDtos;

import java.time.LocalDate;
import java.util.List;

/**
 * Despacho (dispensación) de fórmulas médicas. Es el único proceso que descuenta inventario por fórmula.
 */
public interface DespachoService {

    /**
     * Crea el despacho PENDIENTE_DESPACHO de una fórmula recién emitida (sin tocar inventario).
     *
     * @param formula fórmula emitida.
     * @return despacho creado.
     */
    Despacho crearParaFormula(FormulaMedica formula);

    /**
     * Sincroniza las líneas de un despacho pendiente tras editar la fórmula.
     *
     * @param formula fórmula editada.
     */
    void sincronizarConFormula(FormulaMedica formula);

    /**
     * @param q     texto: id de fórmula, propietario o mascota (opcional).
     * @param fecha fecha de emisión (opcional).
     * @return despachos pendientes, más antiguos primero.
     */
    List<DespachoDtos.Resumen> pendientes(String q, LocalDate fecha);

    /**
     * @param formulaId fórmula.
     * @return detalle con existencias, lotes disponibles, vencidos y próximos a vencer.
     */
    DespachoDtos.Detalle detallePorFormula(Long formulaId);

    /**
     * Pasa el despacho a EN_PREPARACION y devuelve la sugerencia de lotes FEFO.
     *
     * @param id despacho.
     * @return sugerencia.
     */
    DespachoDtos.Sugerencia preparar(Long id);

    /**
     * @param id despacho.
     * @return sugerencia de lotes FEFO (solo vigentes) sin cambiar estado.
     */
    DespachoDtos.Sugerencia sugerir(Long id);

    /**
     * Confirma el despacho: descuenta del lote, registra movimientos SALIDA y el nuevo estado. Transaccional e idempotente.
     *
     * @param id despacho.
     * @param rq cantidades a entregar (opcional).
     * @return detalle actualizado.
     */
    DespachoDtos.Detalle confirmar(Long id, DespachoDtos.ConfirmarRq rq);

    /**
     * Cancela un despacho sin entregas, con autorización de un ADMIN.
     *
     * @param id despacho.
     * @param rq motivo y credenciales del autorizador.
     * @return resumen actualizado.
     */
    DespachoDtos.Resumen cancelar(Long id, DespachoDtos.AutorizacionRq rq);

    /**
     * Reversa un despacho confirmado devolviendo las unidades a sus lotes, con autorización de un ADMIN.
     *
     * @param id despacho.
     * @param rq motivo y credenciales del autorizador.
     * @return resumen actualizado.
     */
    DespachoDtos.Resumen reversar(Long id, DespachoDtos.AutorizacionRq rq);

    /**
     * @param id despacho.
     * @return comprobante PDF de lo entregado.
     */
    byte[] comprobantePdf(Long id);

    /**
     * @return historial de todos los despachos.
     */
    List<DespachoDtos.Resumen> historial();

    /**
     * @param medicamentoId medicamento.
     * @return movimientos de despacho (salidas y reversiones) del medicamento.
     */
    List<DespachoDtos.MovimientoDespacho> historialMedicamento(Long medicamentoId);

    /**
     * @param id despacho.
     * @return historial de cambios de estado.
     */
    List<DespachoEstadoLog> estados(Long id);
}
