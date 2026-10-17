package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Contrato REST para la gestión de fórmulas médicas (prescripciones) emitidas en las citas.
 * No se expone eliminación física: una fórmula emitida hace parte del historial clínico
 * de la mascota y debe conservarse.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/formula-medica")
public interface FormulaMedicaApi {

    /**
     * Lista todas las fórmulas médicas, ordenadas de la fecha
     * de creación más reciente a la más antigua.
     *
     * @return respuesta HTTP con la lista completa de fórmulas médicas.
     * @throws BadRequestException si ocurre un error al procesar la solicitud.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<FormulaMedica>> listarFormulasMedicas()
            throws BadRequestException;

    /**
     * Consulta el detalle completo de una fórmula médica por su identificador.
     *
     * @param formulaId identificador de la fórmula médica.
     * @return respuesta HTTP con la fórmula médica encontrada.
     * @throws BadRequestException si el identificador es inválido o la fórmula no existe.
     */
    @GetMapping(value = "/consultar",
            produces = {"application/json"})
    ResponseEntity<FormulaMedica> consultarFormulaMedica(
            @RequestParam Long formulaId)
            throws BadRequestException;

    /**
     * Lista las citas atendidas de una mascota, que son las consultas sobre las que
     * se puede emitir una fórmula médica. Alimenta el combo dependiente del formulario.
     *
     * @param mascotaId identificador de la mascota.
     * @return respuesta HTTP con las citas atendidas, de la más reciente a la más antigua.
     * @throws BadRequestException si el identificador de la mascota es inválido.
     */
    @GetMapping(value = "/citas-disponibles",
            produces = {"application/json"})
    ResponseEntity<List<Cita>> listarCitasDisponibles(
            @RequestParam Integer mascotaId)
            throws BadRequestException;

    /**
     * Registra una nueva fórmula médica asociada a una cita atendida.
     *
     * @param formulaMedicaRq datos de la fórmula médica a registrar.
     * @return respuesta HTTP con el resultado de la operación.
     * @throws BadRequestException si los datos son inválidos o incumplen las reglas de negocio.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarFormulaMedica(
            @RequestBody FormulaMedicaRq formulaMedicaRq)
            throws BadRequestException;

    /**
     * Actualiza una fórmula médica existente.
     *
     * @param formulaMedicaRq datos actualizados de la fórmula médica (incluye {@code formulaId}).
     * @return respuesta HTTP con el resultado de la operación.
     * @throws BadRequestException si los datos son inválidos, la fórmula no existe
     *                             o incumple las reglas de negocio.
     */
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarFormulaMedica(
            @RequestBody FormulaMedicaRq formulaMedicaRq)
            throws BadRequestException;
}
