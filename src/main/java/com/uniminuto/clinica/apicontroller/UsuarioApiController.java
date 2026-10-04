package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.UsuarioApi;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST que expone la consulta de usuarios del sistema.
 */
@RestController
public class UsuarioApiController implements UsuarioApi {

    /**
     * Servicio con la lógica de negocio de usuarios del sistema.
     */
    @Autowired
    private UsuarioService usuarioService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<Usuario>> listarUsuarios() throws BadRequestException {
        return ResponseEntity.ok(this.usuarioService.obtenerUsuarios());
    }
}
