package com.uniminuto.clinica.reglas;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Despacho;
import com.uniminuto.clinica.entity.DespachoDetalle;
import com.uniminuto.clinica.entity.Factura;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.models.DespachoDtos;
import com.uniminuto.clinica.repository.DespachoRepository;
import com.uniminuto.clinica.repository.FacturaRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.serviceimpl.DespachoServiceImpl;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.TooManyRequestsException;
import com.uniminuto.clinica.exception.UnauthorizedException;
import com.uniminuto.clinica.models.LoginRq;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.security.JwtService;
import com.uniminuto.clinica.security.Roles;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.service.OtpService;
import com.uniminuto.clinica.serviceimpl.AuthServiceImpl;
import com.uniminuto.clinica.serviceimpl.CitaServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Pruebas de las correcciones surgidas de la revisión de punta a punta: bloqueo de login por
 * intentos fallidos, no atender citas futuras y aislamiento entre médicos.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReglasCorreccionesQaTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock OtpService otpService;
    @Mock UsuarioActual usuarioActual;
    @Mock CitaRepository citaRepository;
    @Mock DespachoRepository despachoRepository;
    @Mock FormulaMedicaRepository formulaRepository;
    @Mock FacturaRepository facturaRepository;
    @InjectMocks AuthServiceImpl auth;
    @InjectMocks DespachoServiceImpl despachos;
    @InjectMocks CitaServiceImpl citas;

    private LoginRq login(String user, String pass) {
        LoginRq rq = new LoginRq();
        rq.setUsername(user);
        rq.setPassword(pass);
        return rq;
    }

    @Test
    void tras5FallosElUsuarioQuedaBloqueadoTemporalmente() {
        Usuario u = new Usuario();
        u.setUsername("admin");
        u.setActivo(true);
        u.setPasswordHash("hash");
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);
        for (int i = 0; i < 5; i++) {
            assertThrows(UnauthorizedException.class, () -> auth.login(login("admin", "mala")));
        }
        assertThrows(TooManyRequestsException.class, () -> auth.login(login("admin", "mala")));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        assertThrows(TooManyRequestsException.class, () -> auth.login(login("admin", "buena")));
    }

    @Test
    void noSeAtiendeUnaCitaProgramadaParaUnDiaFuturo() {
        Medico m = new Medico();
        m.setId(1L);
        Cita c = new Cita();
        c.setId(5L);
        c.setEstado("PENDIENTE");
        c.setMedico(m);
        c.setFechaHora(LocalDateTime.now().plusDays(8));
        when(citaRepository.bloquear(5L)).thenReturn(Optional.of(c));
        assertThrows(BadRequestException.class, () -> citas.marcarAtendida(5L));
    }

    @Test
    void unMedicoNoPuedeOperarSobreRegistrosDeOtro() {
        UsuarioActual real = new UsuarioActual() {
            @Override public String rol() { return Roles.MEDICO; }
            @Override public Long medicoId() { return 2L; }
        };
        assertThrows(AccessDeniedException.class, () -> real.exigirPropioSiEsMedico(1L));
    }

    @Test
    void noSeReversaUnaEntregaDeUnaCitaYaFacturada() {
        Usuario admin = new Usuario();
        admin.setUsername("admin");
        admin.setRol(Roles.ADMIN);
        admin.setActivo(true);
        admin.setPasswordHash("hash");
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        Despacho d = new Despacho();
        d.setId(3L);
        d.setFormulaId(4L);
        d.setEstado("DESPACHADO");
        DespachoDetalle linea = new DespachoDetalle();
        linea.setCantidadEntregada(5);
        d.getDetalles().add(linea);
        when(despachoRepository.bloquear(3L)).thenReturn(Optional.of(d));
        Cita cita = new Cita();
        cita.setId(65L);
        FormulaMedica f = new FormulaMedica();
        f.setId(4L);
        f.setCita(cita);
        when(formulaRepository.findById(4L)).thenReturn(Optional.of(f));
        Factura factura = new Factura();
        factura.setNumero("FAC-2026-000043");
        when(facturaRepository.findFirstByCitaIdAndEstadoPagoNot(65L, "ANULADA")).thenReturn(Optional.of(factura));
        assertThrows(ConflictException.class, () -> despachos.reversar(3L,
                new com.uniminuto.clinica.models.DespachoDtos.AutorizacionRq("error", "admin", "x")));
    }
}
