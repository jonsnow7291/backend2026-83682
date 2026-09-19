package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
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
 * Contrato REST para las operaciones relacionadas con citas de la clínica veterinaria.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cita")
public interface CitaApi {

    /**
     * Filtra las citas del sistema dado un rango de fechas, ordenadas de la más
     * reciente a la más antigua.
     *
     * @param fechaInicial límite inferior del rango (inclusive).
     * @param fechaFinal   límite superior del rango (inclusive).
     * @return respuesta HTTP con la lista de citas dentro del rango.
     * @throws BadRequestException si las fechas son nulas o el rango es inválido.
     */
    @GetMapping(value = "/listar-por-fecha",
            produces = {"application/json"})
    ResponseEntity<List<Cita>> listarCitasPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFinal)
            throws BadRequestException;

    /**
     * Registra una nueva cita en el sistema.
     *
     * @param citaRq datos de la cita a registrar.
     * @return respuesta HTTP con el resultado de la operación.
     * @throws BadRequestException si los datos son inválidos o las referencias no existen.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarCita(
            @RequestBody CitaRq citaRq)
            throws BadRequestException;

    /**
     * Actualiza una cita existente en el sistema.
     *
     * @param citaRq datos actualizados de la cita.
     * @return respuesta HTTP con el resultado de la operación.
     * @throws BadRequestException si los datos son inválidos o las referencias no existen.
     */
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarCita(
            @RequestBody CitaRq citaRq)
            throws BadRequestException;
}
