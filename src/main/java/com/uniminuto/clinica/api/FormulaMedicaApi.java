package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Contrato REST de fórmulas médicas. Solo MEDICO/ADMIN crean o editan; DESPACHO_MEDICO no tiene acceso.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/formula-medica")
@Tag(name = "Fórmulas médicas", description = "Fórmulas con varios medicamentos. Crearlas NO descuenta inventario (solo el despacho lo hace)")
public interface FormulaMedicaApi {

    /**
     * @param citaId    filtro opcional.
     * @param mascotaId filtro opcional.
     * @return fórmulas.
     */
    @Operation(summary = "Listar fórmulas")
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<FormulaMedica>> listarFormulasMedicas(@RequestParam(required = false) Long citaId,
                                                              @RequestParam(required = false) Integer mascotaId);

    /**
     * @param formulaId fórmula.
     * @return fórmula con medicamentos.
     * @throws BadRequestException si el id es inválido.
     */
    @Operation(summary = "Consultar fórmula")
    @GetMapping(value = "/consultar", produces = {"application/json"})
    ResponseEntity<FormulaMedica> consultarFormulaMedica(@RequestParam Long formulaId) throws BadRequestException;

    /**
     * @param rq datos.
     * @return confirmación con el id.
     * @throws BadRequestException si falla una validación.
     */
    @Operation(summary = "Crear fórmula", description = "Sobre una cita atendida; varios medicamentos. Crea el despacho PENDIENTE_DESPACHO sin tocar el inventario.")
    @PostMapping(value = "/guardar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarFormulaMedica(@RequestBody FormulaMedicaRq rq) throws BadRequestException;

    /**
     * @param rq datos con id.
     * @return confirmación.
     * @throws BadRequestException si falla una validación.
     */
    @Operation(summary = "Editar fórmula", description = "Solo mientras el despacho siga PENDIENTE_DESPACHO.")
    @PutMapping(value = "/actualizar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarFormulaMedica(@RequestBody FormulaMedicaRq rq) throws BadRequestException;

    /**
     * @param id fórmula.
     * @return disponibilidad por medicamento.
     */
    @Operation(summary = "Disponibilidad de los medicamentos de la fórmula")
    @GetMapping(value = "/{id}/disponibilidad", produces = {"application/json"})
    ResponseEntity<List<Map<String, Object>>> disponibilidad(@PathVariable Long id);

    /**
     * @param id fórmula.
     * @return PDF.
     */
    @Operation(summary = "Fórmula en PDF")
    @GetMapping(value = "/{id}/pdf", produces = {"application/pdf"})
    ResponseEntity<byte[]> pdf(@PathVariable Long id);
}
