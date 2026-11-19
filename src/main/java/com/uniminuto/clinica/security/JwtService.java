package com.uniminuto.clinica.security;

import com.uniminuto.clinica.entity.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Genera los tokens JWT firmados (HS256) que se entregan al iniciar sesión.
 */
@Service
public class JwtService {

    private final JwtEncoder encoder;

    private final long minutosExpiracion;

    /**
     * @param encoder           codificador JWT configurado en {@code SecurityConfig}.
     * @param minutosExpiracion vigencia del token en minutos ({@code app.jwt.expiration-minutes}).
     */
    public JwtService(JwtEncoder encoder, @Value("${app.jwt.expiration-minutes:480}") long minutosExpiracion) {
        this.encoder = encoder;
        this.minutosExpiracion = minutosExpiracion;
    }

    /**
     * Genera un token con el usuario, su rol y (si aplica) el médico asociado.
     *
     * @param usuario  usuario autenticado.
     * @param medicoId id del médico vinculado al usuario, o {@code null}.
     * @return token JWT firmado.
     */
    public String generar(Usuario usuario, Long medicoId) {
        Instant ahora = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .subject(usuario.getUsername())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(Duration.ofMinutes(minutosExpiracion)))
                .claim("uid", usuario.getId())
                .claim("rol", usuario.getRol());
        if (medicoId != null) {
            claims.claim("medicoId", medicoId);
        }
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }

    /**
     * @return vigencia del token en segundos.
     */
    public long segundosExpiracion() {
        return minutosExpiracion * 60;
    }
}
