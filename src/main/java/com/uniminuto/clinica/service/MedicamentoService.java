package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.models.MedicamentoRq;

import java.util.List;

/**
 * Operaciones de negocio sobre productos del inventario (medicamentos y utensilios).
 */
public interface MedicamentoService {

    /**
     * Obtiene todos los registros de medicamentos del inventario.
     *
     * @return lista completa.
     */
    List<Medicamento> obtenerMedicamentos();

    /**
     * Lista productos con filtro de alerta.
     *
     * @param filtro TODOS, AGOTADOS, STOCK_BAJO, PROXIMOS_A_VENCER o VENCIDOS.
     * @param tipo   MEDICAMENTO o UTENSILIO (opcional).
     * @param q      texto a buscar en nombre o código (opcional).
     * @return productos que cumplen el filtro.
     */
    List<Medicamento> listar(String filtro, String tipo, String q);

    /**
     * Consulta un producto.
     *
     * @param id identificador.
     * @return el producto.
     */
    Medicamento consultar(Long id);

    /**
     * Crea un producto (sin existencias; estas entran por lotes).
     *
     * @param rq datos del producto.
     * @return producto creado.
     */
    Medicamento crear(MedicamentoRq rq);

    /**
     * Actualiza los datos de un producto (no su stock).
     *
     * @param rq datos con el id.
     * @return producto actualizado.
     */
    Medicamento actualizar(MedicamentoRq rq);

    /**
     * Activa o inactiva un producto (no se elimina para conservar el histórico).
     *
     * @param id     producto.
     * @param activo nuevo estado.
     * @return producto actualizado.
     */
    Medicamento cambiarEstado(Long id, boolean activo);

    /**
     * Valida un producto y devuelve todos los errores encontrados (vacío si es válido).
     * Se reutiliza en la carga masiva para informar cada fila.
     *
     * @param rq     datos.
     * @param nuevo  {@code true} si es creación (valida duplicados de código y nombre).
     * @return lista de motivos de error.
     */
    List<String> validar(MedicamentoRq rq, boolean nuevo);
}
