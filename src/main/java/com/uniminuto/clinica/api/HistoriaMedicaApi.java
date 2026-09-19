package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato REST para el CRUD de historias médicas de las mascotas.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/historia-medica")
public interface HistoriaMedicaApi {

    /**
     * Lista las historias médicas, ordenadas de la más reciente a la más antigua.
     * Si se envían ambos parámetros de fecha, filtra por ese rango; si no se
     * envía ninguno, lista todas las historias.
     *
     * @param fechaInicial límite inferior del rango (opcional).
     * @param fechaFinal   límite superior del rango (opcional).
     * @return respuesta HTTP con la lista de historias médicas.
     * @throws BadRequestException si se envía solo una de las dos fechas o el rango es inválido.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<HistoriaMedica>> listarHistoriasMedicas(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicial,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFinal)
            throws BadRequestException;

    /**
     * Registra una nueva historia médica para una mascota.
     *
     * @param historiaMedicaRq datos de la historia médica a registrar.
     * @return respuesta HTTP con el resultado de la operación.
     * @throws BadRequestException si los datos son inválidos o la mascota no existe.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarHistoriaMedica(
            @RequestBody HistoriaMedicaRq historiaMedicaRq)
            throws BadRequestException;

    /**
     * Actualiza una historia médica existente.
     *
     * @param historiaMedicaRq datos actualizados de la historia médica.
     * @return respuesta HTTP con el resultado de la operación.
     * @throws BadRequestException si los datos son inválidos, la historia o la mascota no existen.
     */
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarHistoriaMedica(
            @RequestBody HistoriaMedicaRq historiaMedicaRq)
            throws BadRequestException;

    /**
     * Elimina una historia médica existente.
     *
     * @param historiaId identificador de la historia médica a eliminar.
     * @return respuesta HTTP con el resultado de la operación.
     * @throws BadRequestException si el identificador es inválido o la historia no existe.
     */
    @PostMapping(value = "/eliminar",
            produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> eliminarHistoriaMedica(
            @RequestParam Long historiaId)
            throws BadRequestException;
}
