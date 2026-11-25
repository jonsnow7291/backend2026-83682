package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaCalendarioRs;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.ReprogramarRq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Contrato REST de citas médicas. Un usuario MEDICO solo ve y gestiona sus propias citas.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cita")
@Tag(name = "Citas", description = "Agenda, calendario, reprogramación, cancelación y reportes de citas")
public interface CitaApi {

    /**
     * Lista citas por rango de fechas.
     *
     * @param fechaInicial inicio (opcional).
     * @param fechaFinal   fin (opcional).
     * @return citas.
     * @throws BadRequestException si el rango es inválido.
     */
    @Operation(summary = "Listar citas por rango de fechas")
    @GetMapping(value = "/listar-por-fecha", produces = {"application/json"})
    ResponseEntity<List<Cita>> listarCitasPorFecha(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFinal)
            throws BadRequestException;

    /**
     * Consulta una cita.
     *
     * @param citaId identificador.
     * @return la cita.
     * @throws BadRequestException si el id es inválido.
     */
    @Operation(summary = "Consultar una cita")
    @ApiResponse(responseCode = "404", description = "La cita no existe")
    @GetMapping(value = "/consultar", produces = {"application/json"})
    ResponseEntity<Cita> consultarCita(@RequestParam Long citaId) throws BadRequestException;

    /**
     * Citas atendidas de una mascota.
     *
     * @param mascotaId mascota.
     * @return citas atendidas.
     * @throws BadRequestException si la mascota no existe.
     */
    @Operation(summary = "Citas atendidas de una mascota")
    @GetMapping(value = "/listar-atendidas-por-mascota", produces = {"application/json"})
    ResponseEntity<List<Cita>> listarCitasAtendidasPorMascota(@RequestParam Integer mascotaId) throws BadRequestException;

    /**
     * Registra una cita.
     *
     * @param citaRq datos de la cita.
     * @return confirmación.
     * @throws BadRequestException si falla una validación.
     */
    @Operation(summary = "Registrar cita",
            description = "Valida: sin solapamiento del veterinario ni de la mascota (409), médico activo y mascota del propietario. No se restringe por horario del veterinario.")
    @ApiResponse(responseCode = "409", description = "Solapamiento con otra cita")
    @PostMapping(value = "/guardar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarCita(@RequestBody CitaRq citaRq) throws BadRequestException;

    /**
     * Edita una cita pendiente o confirmada.
     *
     * @param citaRq datos con id.
     * @return confirmación.
     * @throws BadRequestException si falla una validación.
     */
    @Operation(summary = "Editar cita pendiente o confirmada")
    @PutMapping(value = "/actualizar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarCita(@RequestBody CitaRq citaRq) throws BadRequestException;

    /**
     * Cancela una cita (conserva el historial).
     *
     * @param citaId identificador.
     * @param motivo motivo (opcional).
     * @return confirmación.
     * @throws BadRequestException si no se puede cancelar.
     */
    @Operation(summary = "Cancelar cita", description = "No elimina la cita: cambia a CANCELADA y guarda el motivo.")
    @PatchMapping(value = "/cancelar", produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> cancelarCita(@RequestParam Long citaId, @RequestParam(required = false) String motivo)
            throws BadRequestException;

    /**
     * Reprograma una cita.
     *
     * @param rq nueva fecha y hora.
     * @return confirmación.
     */
    @Operation(summary = "Reprogramar cita", description = "Aplica las mismas reglas de agenda que al crearla.")
    @PatchMapping(value = "/reprogramar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> reprogramar(@RequestBody ReprogramarRq rq);

    /**
     * Confirma una cita pendiente.
     *
     * @param citaId cita.
     * @return confirmación.
     */
    @Operation(summary = "Confirmar cita")
    @PatchMapping(value = "/confirmar", produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> confirmar(@RequestParam Long citaId);

    /**
     * Marca una cita como atendida.
     *
     * @param citaId cita.
     * @return confirmación.
     */
    @Operation(summary = "Marcar cita como atendida")
    @PatchMapping(value = "/atender", produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> atender(@RequestParam Long citaId);

    /**
     * Datos para el calendario del veterinario.
     *
     * @param medicoId médico (opcional para MEDICO: usa el propio).
     * @param desde    inicio.
     * @param hasta    fin.
     * @return citas listas para pintar (día, semana o mes según el rango).
     */
    @Operation(summary = "Calendario de citas", description = "Devuelve id, título, inicio, fin, estado, mascota, propietario y motivo. El rango define la vista día/semana/mes.")
    @GetMapping(value = "/calendario", produces = {"application/json"})
    ResponseEntity<List<CitaCalendarioRs>> calendario(
            @RequestParam(required = false) Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta);

    /**
     * Historial de citas de una mascota.
     *
     * @param mascotaId mascota.
     * @return citas (incluye canceladas).
     */
    @Operation(summary = "Historial de citas de una mascota")
    @GetMapping(value = "/historial-mascota", produces = {"application/json"})
    ResponseEntity<List<Cita>> historialMascota(@RequestParam Integer mascotaId);

    /**
     * Resumen de citas por estado.
     *
     * @param medicoId médico opcional.
     * @param desde    inicio.
     * @param hasta    fin.
     * @return conteo por estado.
     */
    @Operation(summary = "Resumen de citas por estado")
    @GetMapping(value = "/resumen", produces = {"application/json"})
    ResponseEntity<Map<String, Long>> resumen(
            @RequestParam(required = false) Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta);

    /**
     * Reporte Excel de citas.
     *
     * @param medicoId médico opcional.
     * @param desde    inicio.
     * @param hasta    fin.
     * @return archivo .xlsx.
     */
    @Operation(summary = "Reporte Excel de citas del médico")
    @GetMapping(value = "/reporte-excel")
    ResponseEntity<byte[]> reporteExcel(
            @RequestParam(required = false) Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta);
}
