package com.uniminuto.clinica.security;

import com.uniminuto.clinica.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Acceso al usuario autenticado de la solicitud en curso (resuelto desde el token JWT).
 * Se usa para auditoría (quién realizó cada operación) y para reglas por rol.
 */
@Component
public class UsuarioActual {

    /**
     * Obtiene el token JWT de la solicitud actual.
     *
     * @return el JWT autenticado.
     * @throws UnauthorizedException si no hay un usuario autenticado.
     */
    private Jwt jwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return jwt;
        }
        throw new UnauthorizedException("No hay un usuario autenticado");
    }

    /**
     * @return identificador del usuario autenticado.
     */
    public Long id() {
        return ((Number) jwt().getClaim("uid")).longValue();
    }

    /**
     * @return nombre de usuario autenticado.
     */
    public String username() {
        return jwt().getSubject();
    }

    /**
     * @return rol del usuario autenticado (ver {@link Roles}).
     */
    public String rol() {
        return jwt().getClaimAsString("rol");
    }

    /**
     * @return identificador del médico asociado al usuario, o {@code null} si no es médico.
     */
    public Long medicoId() {
        Object value = jwt().getClaim("medicoId");
        return value == null ? null : ((Number) value).longValue();
    }

    /**
     * @param rol rol a comparar (ver {@link Roles}).
     * @return {@code true} si el usuario autenticado tiene ese rol.
     */
    public boolean tieneRol(String rol) {
        return rol.equals(rol());
    }

    /**
     * @return id del médico si el usuario actual es MEDICO (solo ve lo suyo); {@code null} si no aplica
     *         restricción (otros roles) o no hay sesión (uso interno).
     */
    public Long medicoRestringido() {
        try {
            return tieneRol(Roles.MEDICO) ? medicoId() : null;
        } catch (UnauthorizedException e) {
            return null;
        }
    }

    /**
     * Verifica que el usuario pueda gestionar datos del médico indicado: un usuario MEDICO solo
     * puede operar sobre sus propios datos; el resto de roles clínicos no tiene esa restricción.
     *
     * @param medicoId médico dueño del recurso.
     * @throws org.springframework.security.access.AccessDeniedException si es otro médico.
     */
    public void exigirPropioSiEsMedico(Long medicoId) {
        if (tieneRol(Roles.MEDICO) && (medicoId() == null || !medicoId().equals(medicoId))) {
            throw new org.springframework.security.access.AccessDeniedException("Un médico solo gestiona sus propios registros");
        }
    }
}
