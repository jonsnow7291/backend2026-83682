package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.LoginRq;
import com.uniminuto.clinica.models.LoginRs;
import com.uniminuto.clinica.models.SesionRs;

/**
 * Operaciones de autenticación.
 */
public interface AuthService {

    /**
     * Autentica al usuario y genera el token.
     *
     * @param rq credenciales.
     * @return token y datos de sesión.
     */
    LoginRs login(LoginRq rq);

    /**
     * @return datos del usuario autenticado en la solicitud actual.
     */
    SesionRs sesionActual();
}
