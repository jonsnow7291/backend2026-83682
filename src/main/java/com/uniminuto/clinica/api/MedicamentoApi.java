package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrato REST de productos de inventario (medicamentos y utensilios).
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/medicamento")
@Tag(name = "Inventario - Productos", description = "CRUD de medicamentos/utensilios y alertas de stock")
public interface MedicamentoApi {

    /**
     * Lista todos los productos.
     *
     * @return lista completa.
     * @throws BadRequestException si ocurre un error al procesar la solicitud.
     */
    @Operation(summary = "Listar todos los productos")
    @GetMapping(value = "/listar-todos", produces = {"application/json"})
    ResponseEntity<List<Medicamento>> listarMedicamentos() throws BadRequestException;

    /**
     * Lista productos con filtros de alerta.
     *
     * @param filtro TODOS, AGOTADOS, STOCK_BAJO, PROXIMOS_A_VENCER o VENCIDOS.
     * @param tipo   MEDICAMENTO o UTENSILIO.
     * @param q      texto en nombre o código.
     * @return productos filtrados.
     */
    @Operation(summary = "Listar productos filtrables", description = "Filtros: TODOS, AGOTADOS, STOCK_BAJO, PROXIMOS_A_VENCER (30 días), VENCIDOS")
    @ApiResponse(responseCode = "400", description = "Filtro inválido")
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<Medicamento>> listar(
            @Parameter(description = "Filtro de alerta") @RequestParam(required = false) String filtro,
            @Parameter(description = "MEDICAMENTO o UTENSILIO") @RequestParam(required = false) String tipo,
            @Parameter(description = "Búsqueda por nombre o código") @RequestParam(required = false) String q);

    /**
     * Consulta un producto.
     *
     * @param id identificador.
     * @return el producto.
     */
    @Operation(summary = "Consultar un producto")
    @ApiResponse(responseCode = "404", description = "No existe")
    @GetMapping(value = "/consultar", produces = {"application/json"})
    ResponseEntity<Medicamento> consultar(@RequestParam Long id);

    /**
     * Crea un producto.
     *
     * @param rq datos del producto.
     * @return producto creado.
     */
    @Operation(summary = "Crear producto", description = "Código de 8 alfanuméricos único; Invima obligatorio si es MEDICAMENTO. El stock entra por lotes (/inventario/entrada).")
    @ApiResponse(responseCode = "400", description = "Validación fallida")
    @PostMapping(value = "/guardar", consumes = {"application/json"}, produces = {"application/json"})
    ResponseEntity<Medicamento> crear(@RequestBody MedicamentoRq rq);

    /**
     * Actualiza un producto.
     *
     * @param rq datos con id.
     * @return producto actualizado.
     */
    @Operation(summary = "Actualizar producto", description = "No modifica existencias.")
    @PutMapping(value = "/actualizar", consumes = {"application/json"}, produces = {"application/json"})
    ResponseEntity<Medicamento> actualizar(@RequestBody MedicamentoRq rq);

    /**
     * Activa o inactiva un producto.
     *
     * @param id     producto.
     * @param activo nuevo estado.
     * @return producto actualizado.
     */
    @Operation(summary = "Activar o inactivar producto", description = "Los productos no se eliminan para conservar el histórico.")
    @PatchMapping(value = "/estado", produces = {"application/json"})
    ResponseEntity<Medicamento> cambiarEstado(@RequestParam Long id, @RequestParam boolean activo);
}
