package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.AuthApi;
import com.uniminuto.clinica.models.LoginRq;
import com.uniminuto.clinica.models.LoginRs;
import com.uniminuto.clinica.models.SesionRs;
import com.uniminuto.clinica.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de autenticación; delega en {@link AuthService}.
 */
@RestController
public class AuthApiController implements AuthApi {

    @Autowired
    private AuthService authService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<LoginRs> login(LoginRq rq) {
        return ResponseEntity.ok(authService.login(rq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<SesionRs> me() {
        return ResponseEntity.ok(authService.sesionActual());
    }
}
