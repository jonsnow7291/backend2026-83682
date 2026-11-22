package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.UnauthorizedException;
import com.uniminuto.clinica.models.LoginRq;
import com.uniminuto.clinica.models.LoginRs;
import com.uniminuto.clinica.models.SesionRs;
import com.uniminuto.clinica.models.VerificarOtpRq;
import com.uniminuto.clinica.service.OtpService;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.security.JwtService;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementación del login: valida usuario activo y contraseña (BCrypt) y emite el JWT.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final String MENSAJE_INVALIDO = "Usuario o contraseña incorrectos";

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private UsuarioActual usuarioActual;

    /** {@inheritDoc} */
    @Override
    public LoginRs login(LoginRq rq) {
        Usuario usuario = usuarioRepository.findByUsername(rq.getUsername().trim())
                .orElseThrow(() -> new UnauthorizedException(MENSAJE_INVALIDO));
        if (!Boolean.TRUE.equals(usuario.getActivo())
                || usuario.getPasswordHash() == null
                || !passwordEncoder.matches(rq.getPassword(), usuario.getPasswordHash())) {
            throw new UnauthorizedException(MENSAJE_INVALIDO);
        }
        if (otpService.requiere(usuario)) {
            String desafio = otpService.emitir(usuario);
            return new LoginRs(null, "Bearer", 0, null, true, desafio, otpService.enmascarar(usuario.getEmail()));
        }
        return sesion(usuario);
    }

    /** {@inheritDoc} */
    @Override
    public LoginRs verificarOtp(VerificarOtpRq rq) {
        String username = otpService.verificar(rq.getDesafioId(), rq.getCodigo());
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException(MENSAJE_INVALIDO));
        return sesion(usuario);
    }

    private LoginRs sesion(Usuario usuario) {
        Long medicoId = medicoRepository.findByUsuarioId(usuario.getId()).map(Medico::getId).orElse(null);
        String token = jwtService.generar(usuario, medicoId);
        return new LoginRs(token, "Bearer", jwtService.segundosExpiracion(),
                new SesionRs(usuario.getId(), usuario.getUsername(), usuario.getRol(), medicoId));
    }

    /** {@inheritDoc} */
    @Override
    public SesionRs sesionActual() {
        return new SesionRs(usuarioActual.id(), usuarioActual.username(), usuarioActual.rol(), usuarioActual.medicoId());
    }
}
