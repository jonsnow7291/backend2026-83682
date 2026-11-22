package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.UsuarioApi;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.service.UsuarioService;
import com.uniminuto.clinica.models.UsuarioRq;
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

    @Override
    public ResponseEntity<Usuario> guardarUsuario(UsuarioRq rq) {
        return ResponseEntity.ok(this.usuarioService.guardarUsuario(rq));
    }

    @Override
    public ResponseEntity<Usuario> actualizarUsuario(UsuarioRq rq) {
        return ResponseEntity.ok(this.usuarioService.actualizarUsuario(rq));
    }

    @Override
    public ResponseEntity<Usuario> cambiarEstado(Long id, boolean activo) {
        return ResponseEntity.ok(this.usuarioService.cambiarEstado(id, activo));
    }
}
