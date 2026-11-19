package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.AtencionMedica;
import com.uniminuto.clinica.models.AtencionRq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrato REST de atención médica.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/atencion")
@Tag(name = "Atención médica", description = "Apertura desde una cita, registro clínico, finalización y prefactura")
public interface AtencionApi {

    /**
     * @param rq cita y datos iniciales.
     * @return atención abierta.
     */
    @Operation(summary = "Abrir atención desde una cita", description = "La cita pasa a ATENDIDA. Una cita solo tiene una atención.")
    @PostMapping(value = "/abrir", consumes = {"application/json"}, produces = {"application/json"})
    ResponseEntity<AtencionMedica> abrir(@RequestBody AtencionRq rq);

    /**
     * @param rq datos clínicos con id.
     * @return atención actualizada.
     */
    @Operation(summary = "Registrar síntomas, diagnóstico, observaciones y recomendaciones")
    @PutMapping(value = "/registrar", consumes = {"application/json"}, produces = {"application/json"})
    ResponseEntity<AtencionMedica> registrar(@RequestBody AtencionRq rq);

    /**
     * @param id atención.
     * @return atención finalizada.
     */
    @Operation(summary = "Finalizar atención", description = "Requiere diagnóstico. La cita pasa a FINALIZADA y queda en la historia clínica de la mascota.")
    @PatchMapping(value = "/finalizar", produces = {"application/json"})
    ResponseEntity<AtencionMedica> finalizar(@RequestParam Long id);

    /**
     * @param id atención.
     * @return la atención.
     */
    @Operation(summary = "Consultar atención")
    @GetMapping(value = "/consultar", produces = {"application/json"})
    ResponseEntity<AtencionMedica> consultar(@RequestParam Long id);

    /**
     * @param mascotaId mascota.
     * @return atenciones de la mascota.
     */
    @Operation(summary = "Historial clínico de una mascota")
    @GetMapping(value = "/historial-mascota", produces = {"application/json"})
    ResponseEntity<List<AtencionMedica>> historialMascota(@RequestParam Integer mascotaId);

    /**
     * @param id atención.
     * @return PDF de la prefactura.
     */
    @Operation(summary = "Prefactura en PDF", description = "Servicios y medicamentos formulados con valores estimados.")
    @GetMapping(value = "/{id}/prefactura-pdf", produces = {"application/pdf"})
    ResponseEntity<byte[]> prefacturaPdf(@PathVariable Long id);
}
