package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;
import java.util.Map;

/**
 * Fórmulas médicas. Crear una fórmula NO descuenta inventario; solo genera el despacho pendiente.
 */
public interface FormulaMedicaService {

    /**
     * @param citaId    filtro opcional por cita.
     * @param mascotaId filtro opcional por mascota.
     * @return fórmulas.
     */
    List<FormulaMedica> listarFormulasMedicas(Long citaId, Integer mascotaId);

    /**
     * @param formulaId fórmula.
     * @return la fórmula con sus medicamentos.
     * @throws BadRequestException si el id es inválido.
     */
    FormulaMedica consultarFormulaMedica(Long formulaId) throws BadRequestException;

    /**
     * Confirma una fórmula en BORRADOR (regla 5.8-2: el médico crea y confirma). Al confirmarla se
     * crea su despacho PENDIENTE_DESPACHO; el inventario no se toca.
     *
     * @param formulaId fórmula a confirmar.
     * @return respuesta de éxito.
     * @throws BadRequestException si la fórmula ya estaba confirmada o el id es inválido.
     */
    MiRespuestaRS confirmarFormulaMedica(Long formulaId) throws BadRequestException;

    /**
     * Emite una fórmula (en estado BORRADOR) con varios medicamentos. No genera despacho hasta confirmarla.
     *
     * @param rq datos.
     * @return respuesta con el id de la fórmula en el mensaje.
     * @throws BadRequestException si falla una validación.
     */
    MiRespuestaRS guardarFormulaMedica(FormulaMedicaRq rq) throws BadRequestException;

    /**
     * Edita una fórmula mientras sea BORRADOR o su despacho siga PENDIENTE_DESPACHO.
     *
     * @param rq datos con id.
     * @return respuesta de éxito.
     * @throws BadRequestException si falla una validación.
     */
    MiRespuestaRS actualizarFormulaMedica(FormulaMedicaRq rq) throws BadRequestException;

    /**
     * Disponibilidad de cada medicamento de la fórmula.
     *
     * @param formulaId fórmula.
     * @return lista con medicamento, prescrito, stock vigente y si alcanza.
     */
    List<Map<String, Object>> disponibilidad(Long formulaId);

    /**
     * @param formulaId fórmula.
     * @return PDF de la fórmula.
     */
    byte[] pdf(Long formulaId);
}
