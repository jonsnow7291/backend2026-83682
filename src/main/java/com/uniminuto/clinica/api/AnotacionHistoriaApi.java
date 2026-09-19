package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
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
 * Contrato REST para la gestión de anotaciones dentro de una historia médica.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/anotacion-historia")
public interface AnotacionHistoriaApi {

    /**
     * Lista las anotaciones de una historia médica específica, ordenadas
     * de la más reciente a la más antigua.
     *
     * @param historiaId identificador de la historia médica.
     * @return respuesta HTTP con la lista de anotaciones de la historia médica indicada.
     * @throws BadRequestException si el identificador es inválido o la historia no existe.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<AnotacionHistoria>> listarAnotaciones(
            @RequestParam Long historiaId)
            throws BadRequestException;

    /**
     * Registra una nueva anotación en una historia médica.
     *
     * @param anotacionHistoriaRq datos de la anotación a registrar.
     * @return respuesta HTTP con el resultado de la operación.
     * @throws BadRequestException si los datos son inválidos o las referencias no existen.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarAnotacionHistoria(
            @RequestBody AnotacionHistoriaRq anotacionHistoriaRq)
            throws BadRequestException;

    /**
     * Actualiza una anotación de historia médica existente.
     *
     * @param anotacionHistoriaRq datos actualizados de la anotación.
     * @return respuesta HTTP con el resultado de la operación.
     * @throws BadRequestException si los datos son inválidos o las referencias no existen.
     */
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarAnotacionHistoria(
            @RequestBody AnotacionHistoriaRq anotacionHistoriaRq)
            throws BadRequestException;
}
