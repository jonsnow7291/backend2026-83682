package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.UnauthorizedException;
import com.uniminuto.clinica.service.OtpService;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * OTP de 6 dígitos enviado por SMTP. Los desafíos viven en memoria (una sola instancia del backend),
 * caducan a los 5 minutos y se anulan tras 5 intentos fallidos.
 */
@Service
public class OtpServiceImpl implements OtpService {

    private static final Logger LOG = LoggerFactory.getLogger(OtpServiceImpl.class);
    private static final int MINUTOS_VIGENCIA = 5;
    private static final int MAX_INTENTOS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private record Desafio(String username, String codigo, Instant expira, int[] intentos) { }

    private final Map<String, Desafio> desafios = new ConcurrentHashMap<>();

    @Autowired
    private JavaMailSender mailSender;

    /** Usuarios con segundo factor (lista separada por comas). Vacío = nadie. */
    @Value("${app.otp.usuarios:}")
    private String usuariosOtp;

    /** Remitente verificado en Brevo. */
    @Value("${app.mail.remitente:}")
    private String remitente;

    /** {@inheritDoc} */
    @Override
    public boolean requiere(Usuario usuario) {
        Set<String> lista = Arrays.stream(usuariosOtp.split(",")).map(String::trim)
                .filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        return lista.contains(usuario.getUsername());
    }

    /** {@inheritDoc} */
    @Override
    public String emitir(Usuario usuario) {
        desafios.values().removeIf(d -> d.expira().isBefore(Instant.now()));
        String codigo = String.format("%06d", RANDOM.nextInt(1_000_000));
        String id = UUID.randomUUID().toString();
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(usuario.getEmail());
        if (!remitente.isBlank()) {
            mensaje.setFrom(remitente);
        }
        mensaje.setSubject("Código de acceso - Clínica veterinaria");
        mensaje.setText("Su código de acceso es: " + codigo + "\n\nVence en " + MINUTOS_VIGENCIA
                + " minutos. Si usted no intentó iniciar sesión, ignore este mensaje.");
        try {
            mailSender.send(mensaje);
        } catch (Exception e) {
            LOG.error("No se pudo enviar el OTP a {}: {}", enmascarar(usuario.getEmail()), e.getMessage());
            throw new IllegalStateException("No se pudo enviar el código por correo. Intente de nuevo.");
        }
        desafios.put(id, new Desafio(usuario.getUsername(), codigo,
                Instant.now().plusSeconds(MINUTOS_VIGENCIA * 60L), new int[]{0}));
        return id;
    }

    /** {@inheritDoc} */
    @Override
    public String verificar(String desafioId, String codigo) {
        Desafio d = desafios.get(desafioId);
        if (d == null || d.expira().isBefore(Instant.now())) {
            desafios.remove(desafioId);
            throw new UnauthorizedException("El código venció. Inicie sesión de nuevo.");
        }
        if (!d.codigo().equals(codigo == null ? "" : codigo.trim())) {
            if (++d.intentos()[0] >= MAX_INTENTOS) {
                desafios.remove(desafioId);
                throw new UnauthorizedException("Demasiados intentos. Inicie sesión de nuevo.");
            }
            throw new UnauthorizedException("Código incorrecto");
        }
        desafios.remove(desafioId);
        return d.username();
    }

    /** {@inheritDoc} */
    @Override
    public String enmascarar(String correo) {
        int at = correo.indexOf('@');
        return at <= 1 ? correo : correo.charAt(0) + "***" + correo.substring(at);
    }
}
