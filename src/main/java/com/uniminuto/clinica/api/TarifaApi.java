package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Tarifa;
import com.uniminuto.clinica.entity.TarifaLog;
import com.uniminuto.clinica.models.TarifaRq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrato REST de tarifas. La escritura es exclusiva del ADMIN.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/tarifa")
@Tag(name = "Tarifas", description = "Valores de servicios por especialidad con historial de cambios (solo ADMIN modifica)")
public interface TarifaApi {

    /**
     * @param especializacionId filtro opcional.
     * @param todas             incluir inactivas.
     * @return tarifas.
     */
    @Operation(summary = "Listar tarifas")
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<Tarifa>> listar(@RequestParam(required = false) Long especializacionId,
                                        @RequestParam(defaultValue = "false") boolean todas);

    /**
     * @param rq datos.
     * @return tarifa creada.
     */
    @Operation(summary = "Crear tarifa (ADMIN)")
    @PostMapping(value = "/guardar", consumes = {"application/json"}, produces = {"application/json"})
    ResponseEntity<Tarifa> crear(@RequestBody TarifaRq rq);

    /**
     * @param rq datos con id.
     * @return tarifa actualizada.
     */
    @Operation(summary = "Modificar tarifa (ADMIN)", description = "Registra valor anterior y nuevo, usuario y fecha en tarifa_log. No altera facturas ya emitidas.")
    @PutMapping(value = "/actualizar", consumes = {"application/json"}, produces = {"application/json"})
    ResponseEntity<Tarifa> actualizar(@RequestBody TarifaRq rq);

    /**
     * @param id     tarifa.
     * @param activa nuevo estado.
     * @return tarifa actualizada.
     */
    @Operation(summary = "Activar o inactivar tarifa (ADMIN)")
    @PatchMapping(value = "/estado", produces = {"application/json"})
    ResponseEntity<Tarifa> cambiarEstado(@RequestParam Long id, @RequestParam boolean activa);

    /**
     * @param id tarifa.
     * @return historial.
     */
    @Operation(summary = "Historial de cambios de una tarifa")
    @GetMapping(value = "/historial", produces = {"application/json"})
    ResponseEntity<List<TarifaLog>> historial(@RequestParam Long id);
}
