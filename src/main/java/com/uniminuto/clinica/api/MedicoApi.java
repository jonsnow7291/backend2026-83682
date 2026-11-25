package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.uniminuto.clinica.models.MedicoRq;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Contrato REST para la consulta de médicos veterinarios.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/medico")
public interface MedicoApi {

    /**
     * Lista todos los registros de médicos veterinarios.
     *
     * @return respuesta HTTP con la lista completa.
     * @throws BadRequestException si ocurre un error al procesar la solicitud.
     */
    @GetMapping(value = "/listar-todos",
            produces = {"application/json"})
    ResponseEntity<List<Medico>> listarMedicos()
            throws BadRequestException;

    /**
     * Activa o inactiva el registro.
     *
     * @param id     identificador.
     * @param activo nuevo estado.
     * @return registro actualizado.
     */
    @io.swagger.v3.oas.annotations.Operation(summary = "Registrar un médico veterinario")
    @PostMapping(value = "/guardar", consumes = {"application/json"}, produces = {"application/json"})
    ResponseEntity<Medico> guardarMedico(@RequestBody MedicoRq rq);

    @io.swagger.v3.oas.annotations.Operation(summary = "Actualizar un médico veterinario")
    @PutMapping(value = "/actualizar", consumes = {"application/json"}, produces = {"application/json"})
    ResponseEntity<Medico> actualizarMedico(@RequestBody MedicoRq rq);

    @io.swagger.v3.oas.annotations.Operation(summary = "Activar o inactivar")
    @org.springframework.web.bind.annotation.PatchMapping(value = "/estado", produces = {"application/json"})
    ResponseEntity<Medico> cambiarEstado(@org.springframework.web.bind.annotation.RequestParam Long id,
                                        @org.springframework.web.bind.annotation.RequestParam boolean activo);

    /** Crea una cuenta de acceso (rol MEDICO) vinculada a un médico existente que no tenga usuario. */
    @io.swagger.v3.oas.annotations.Operation(summary = "Crear acceso de usuario para médico")
    @PostMapping(value = "/crear-acceso", produces = {"application/json"})
    ResponseEntity<com.uniminuto.clinica.models.MiRespuestaRS> crearAccesoUsuario(
            @RequestParam Long id)
            throws BadRequestException;

    /** Elimina un médico (y su usuario asociado si existe). Solo ADMIN. */
    @io.swagger.v3.oas.annotations.Operation(summary = "Eliminar médico")
    @DeleteMapping(value = "/eliminar", produces = {"application/json"})
    ResponseEntity<com.uniminuto.clinica.models.MiRespuestaRS> eliminarMedico(
            @RequestParam Long id)
            throws BadRequestException;
}
