package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.LoteMedicamento;
import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.models.LoteRq;

import java.util.List;

/**
 * Lógica de inventario: lotes, entradas, salidas y sincronización de existencias.
 */
public interface InventarioService {

    /**
     * Registra la entrada de un lote (genera movimiento ENTRADA y actualiza el stock).
     *
     * @param rq datos del lote.
     * @return lote creado.
     */
    LoteMedicamento registrarEntrada(LoteRq rq);

    /**
     * Lista los lotes de un medicamento (el que vence primero al inicio).
     *
     * @param medicamentoId medicamento.
     * @return lotes.
     */
    List<LoteMedicamento> listarLotes(Long medicamentoId);

    /**
     * Historial de movimientos.
     *
     * @param medicamentoId filtro opcional por medicamento.
     * @return movimientos, más reciente primero.
     */
    List<MovimientoInventario> listarMovimientos(Long medicamentoId);

    /**
     * Registra una SALIDA sobre un lote (descuento por despacho) validando existencias y vigencia.
     *
     * @param loteId     lote del que sale.
     * @param cantidad   unidades a descontar (mayor que cero).
     * @param formulaId  fórmula asociada.
     * @param despachoId despacho asociado.
     * @param motivo     descripción del movimiento.
     */
    void registrarSalida(Long loteId, int cantidad, Long formulaId, Long despachoId, String motivo);

    /**
     * Devuelve unidades a un lote (reversión de un despacho) con movimiento ENTRADA.
     *
     * @param loteId     lote que recupera existencias.
     * @param cantidad   unidades.
     * @param formulaId  fórmula asociada.
     * @param despachoId despacho revertido.
     * @param motivo     motivo.
     */
    void registrarReversion(Long loteId, int cantidad, Long formulaId, Long despachoId, String motivo);

    /**
     * Recalcula {@code stock_actual} como la suma de lotes vigentes.
     *
     * @param medicamentoId medicamento.
     */
    void sincronizarStock(Long medicamentoId);
}
