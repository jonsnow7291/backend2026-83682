package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.uniminuto.clinica.models.UsuarioRq;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Contrato REST para la consulta de usuarios del sistema.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/usuario")
public interface UsuarioApi {

    /**
     * Lista todos los registros de usuarios del sistema.
     *
     * @return respuesta HTTP con la lista completa.
     * @throws BadRequestException si ocurre un error al procesar la solicitud.
     */
    @GetMapping(value = "/listar-todos",
            produces = {"application/json"})
    ResponseEntity<List<Usuario>> listarUsuarios()
            throws BadRequestException;

    @PostMapping(value = "/guardar", consumes = {"application/json"}, produces = {"application/json"})
    ResponseEntity<Usuario> guardarUsuario(@RequestBody UsuarioRq rq);

    @PutMapping(value = "/actualizar", consumes = {"application/json"}, produces = {"application/json"})
    ResponseEntity<Usuario> actualizarUsuario(@RequestBody UsuarioRq rq);

    @PatchMapping(value = "/estado", produces = {"application/json"})
    ResponseEntity<Usuario> cambiarEstado(@RequestParam Long id, @RequestParam boolean activo);

    /** Elimina un usuario y su médico asociado si existe. Solo ADMIN. */
    @DeleteMapping(value = "/eliminar", produces = {"application/json"})
    ResponseEntity<com.uniminuto.clinica.models.MiRespuestaRS> eliminarUsuario(@RequestParam Long id)
            throws BadRequestException;
}
