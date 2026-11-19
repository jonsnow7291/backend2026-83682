package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.LoginRq;
import com.uniminuto.clinica.models.LoginRs;
import com.uniminuto.clinica.models.SesionRs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Contrato REST de autenticación.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Inicio de sesión y datos de la sesión actual")
public interface AuthApi {

    /**
     * Valida las credenciales y entrega un token JWT.
     *
     * @param rq usuario y contraseña.
     * @return token y datos del usuario.
     */
    @Operation(summary = "Iniciar sesión", description = "Devuelve un token JWT con el rol del usuario. Usarlo como 'Authorization: Bearer <token>'.")
    @ApiResponse(responseCode = "200", description = "Autenticado")
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas o usuario inactivo")
    @PostMapping(value = "/login", consumes = {"application/json"}, produces = {"application/json"})
    ResponseEntity<LoginRs> login(@Valid @RequestBody LoginRq rq);

    /**
     * Devuelve los datos del usuario del token enviado.
     *
     * @return usuario autenticado.
     */
    @Operation(summary = "Sesión actual", description = "Usuario, rol y médico asociado del token enviado.")
    @ApiResponse(responseCode = "200", description = "Sesión vigente")
    @ApiResponse(responseCode = "401", description = "Token ausente o vencido")
    @GetMapping(value = "/me", produces = {"application/json"})
    ResponseEntity<SesionRs> me();
}
