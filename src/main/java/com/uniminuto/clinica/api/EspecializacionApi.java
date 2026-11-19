package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Contrato REST para la consulta de especializaciones médicas.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/especializacion")
public interface EspecializacionApi {

    /**
     * Lista todos los registros de especializaciones médicas.
     *
     * @return respuesta HTTP con la lista completa.
     * @throws BadRequestException si ocurre un error al procesar la solicitud.
     */
    @GetMapping(value = "/listar-todos",
            produces = {"application/json"})
    ResponseEntity<List<Especializacion>> listarEspecializaciones()
            throws BadRequestException;

    /**
     * Activa o inactiva el registro.
     *
     * @param id     identificador.
     * @param activo nuevo estado.
     * @return registro actualizado.
     */
    @io.swagger.v3.oas.annotations.Operation(summary = "Activar o inactivar")
    @org.springframework.web.bind.annotation.PatchMapping(value = "/estado", produces = {"application/json"})
    ResponseEntity<Especializacion> cambiarEstado(@org.springframework.web.bind.annotation.RequestParam Long id,
                                        @org.springframework.web.bind.annotation.RequestParam boolean activo);
}
