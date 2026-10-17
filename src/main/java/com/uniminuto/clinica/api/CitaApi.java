package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
     * Filtra las citas del sistema dado un rango de fechas opcional, ordenadas de la más
     * reciente a la más antigua. Si no se envían fechas, lista todas las citas.
     *
     * @param fechaInicial límite inferior del rango (inclusive, opcional).
     * @param fechaFinal   límite superior del rango (inclusive, opcional).
     * @return respuesta HTTP con la lista de citas dentro del rango o todas las registradas.
     * @throws BadRequestException si solo se envía una fecha o la fecha inicial es posterior a la final.
     */
    @GetMapping(value = "/listar-por-fecha",
            produces = {"application/json"})
    ResponseEntity<List<Cita>> listarCitasPorFecha(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicial,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFinal)
            throws BadRequestException;

    /**
     * Consulta el detalle de una cita médica específica por su identificador único.
     *
     * @param citaId identificador de la cita a consultar.
     * @return respuesta HTTP con la entidad Cita encontrada.
     * @throws BadRequestException si el identificador es nulo o la cita no existe.
     */
    @GetMapping(value = "/consultar",
            produces = {"application/json"})
    ResponseEntity<Cita> consultarCita(
            @RequestParam Long citaId)
            throws BadRequestException;

    /**
     * Lista las citas atendidas de una mascota, de la más reciente a la más antigua.
     * Alimenta los combos dependientes de historias clínicas y fórmulas médicas
     * (Cliente -> Mascota -> Cita atendida).
     *
     * @param mascotaId identificador de la mascota.
     * @return respuesta HTTP con las citas atendidas de la mascota.
     * @throws BadRequestException si el identificador de la mascota es inválido o no existe.
     */
    @GetMapping(value = "/listar-atendidas-por-mascota",
            produces = {"application/json"})
    ResponseEntity<List<Cita>> listarCitasAtendidasPorMascota(
            @RequestParam Integer mascotaId)
            throws BadRequestException;

    /**
     * Registra una nueva cita en el sistema validando todas las reglas de negocio críticas.
     *
     * @param citaRq datos de la cita a registrar.
     * @return respuesta HTTP con el resultado de la operación.
     * @throws BadRequestException si los datos son inválidos, hay solapamiento de horario o las referencias no existen.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarCita(
            @RequestBody CitaRq citaRq)
            throws BadRequestException;

    /**
     * Actualiza una cita existente en el sistema teniendo en cuenta su estado actual.
     *
     * @param citaRq datos actualizados de la cita.
     * @return respuesta HTTP con el resultado de la operación.
     * @throws BadRequestException si la cita está cancelada/atendida o las referencias no coinciden.
     */
    @PutMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarCita(
            @RequestBody CitaRq citaRq)
            throws BadRequestException;

    /**
     * Cancela una cita médica explícitamente y actualiza su estado a 'Cancelada'.
     *
     * @param citaId identificador de la cita a cancelar.
     * @return respuesta HTTP con el resultado de la operación.
     * @throws BadRequestException si la cita no existe o ya fue atendida/cancelada.
     */
    @PatchMapping(value = "/cancelar",
            produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> cancelarCita(
            @RequestParam Long citaId)
            throws BadRequestException;
}
