package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Usuario;

/**
 * Segundo factor por correo (código de un solo uso).
 */
public interface OtpService {

    /** @return {@code true} si el usuario debe pasar por OTP. */
    boolean requiere(Usuario usuario);

    /**
     * Genera un código, lo envía al correo del usuario y devuelve el identificador del desafío.
     *
     * @param usuario usuario que ya validó su contraseña.
     * @return identificador del desafío.
     */
    String emitir(Usuario usuario);

    /**
     * Valida el código y consume el desafío.
     *
     * @param desafioId identificador del desafío.
     * @param codigo código ingresado.
     * @return nombre de usuario dueño del desafío.
     */
    String verificar(String desafioId, String codigo);

    /** @return el correo con la parte local enmascarada (j***@gmail.com). */
    String enmascarar(String correo);
}
