package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.HorarioVeterinario;
import com.uniminuto.clinica.models.HorarioRq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrato REST de horarios de disponibilidad de veterinarios.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/horario-veterinario")
@Tag(name = "Horarios de veterinarios", description = "Disponibilidad semanal usada para validar las citas")
public interface HorarioVeterinarioApi {

    /**
     * @param medicoId médico.
     * @return horarios del médico.
     */
    @Operation(summary = "Listar horarios de un veterinario")
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<HorarioVeterinario>> listar(@RequestParam Long medicoId);

    /**
     * @param rq datos de la franja.
     * @return franja creada.
     */
    @Operation(summary = "Crear franja de horario", description = "Día ISO 1=lunes..7=domingo, horas HH:mm. No puede cruzarse con otra franja activa.")
    @PostMapping(value = "/guardar", consumes = {"application/json"}, produces = {"application/json"})
    ResponseEntity<HorarioVeterinario> crear(@RequestBody HorarioRq rq);

    /**
     * @param rq datos con id.
     * @return franja actualizada.
     */
    @Operation(summary = "Actualizar franja de horario")
    @PutMapping(value = "/actualizar", consumes = {"application/json"}, produces = {"application/json"})
    ResponseEntity<HorarioVeterinario> actualizar(@RequestBody HorarioRq rq);

    /**
     * @param id franja.
     * @return sin contenido.
     */
    @Operation(summary = "Eliminar franja de horario")
    @DeleteMapping("/eliminar")
    ResponseEntity<Void> eliminar(@RequestParam Long id);
}
