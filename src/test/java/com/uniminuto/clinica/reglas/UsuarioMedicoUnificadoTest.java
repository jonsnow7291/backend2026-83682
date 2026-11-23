package com.uniminuto.clinica.reglas;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.service.MedicoService;
import com.uniminuto.clinica.serviceimpl.UsuarioServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Alta y edición unificadas de usuario y médico: un médico se gestiona desde el formulario de usuarios.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsuarioMedicoUnificadoTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock MedicoRepository medicoRepository;
    @Mock MedicoService medicoService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock UsuarioActual usuarioActual;
    @InjectMocks UsuarioServiceImpl usuarios;

    private UsuarioRq rqMedico(MedicoRq medico) {
        UsuarioRq rq = new UsuarioRq();
        rq.setUsername("vet.nuevo");
        rq.setPassword("Clinica2026*");
        rq.setEmail("vet@clinica.com");
        rq.setRol("MEDICO");
        rq.setMedico(medico);
        return rq;
    }

    @Test
    void usuarioMedicoSinDatosProfesionalesSeRechaza() {
        when(usuarioRepository.save(any())).thenAnswer(i -> {
            Usuario u = i.getArgument(0);
            u.setId(20L);
            return u;
        });
        assertThrows(BadRequestException.class, () -> usuarios.guardarUsuario(rqMedico(null)));
    }

    @Test
    void altaDeMedicoCreaElMedicoVinculadoAlUsuario() {
        when(usuarioRepository.save(any())).thenAnswer(i -> {
            Usuario u = i.getArgument(0);
            u.setId(20L);
            return u;
        });
        usuarios.guardarUsuario(rqMedico(new MedicoRq()));
        ArgumentCaptor<MedicoRq> cap = ArgumentCaptor.forClass(MedicoRq.class);
        verify(medicoService).guardarMedico(cap.capture());
        assertEquals(20L, cap.getValue().getUsuarioId());
    }

    @Test
    void usuarioConMedicoAsociadoNoPuedeCambiarDeRol() {
        Usuario u = new Usuario();
        u.setId(5L);
        u.setUsername("medico1");
        u.setRol("MEDICO");
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(u));
        when(usuarioActual.id()).thenReturn(3L);
        Medico m = new Medico();
        m.setId(1L);
        when(medicoRepository.findByUsuarioId(5L)).thenReturn(Optional.of(m));
        UsuarioRq rq = new UsuarioRq();
        rq.setId(5L);
        rq.setUsername("medico1");
        rq.setEmail("m@clinica.com");
        rq.setRol("SECRETARIA");
        assertThrows(ConflictException.class, () -> usuarios.actualizarUsuario(rq));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void inactivarUnUsuarioMedicoInactivaTambienAlMedico() {
        Usuario u = new Usuario();
        u.setId(5L);
        u.setActivo(true);
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(u));
        when(usuarioActual.id()).thenReturn(3L);
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        Medico m = new Medico();
        m.setId(1L);
        when(medicoRepository.findByUsuarioId(5L)).thenReturn(Optional.of(m));
        usuarios.cambiarEstado(5L, false);
        verify(medicoService).cambiarEstado(1L, false);
    }

    @Test
    void noSePuedeCambiarElPropioNombreDeUsuario() {
        Usuario u = new Usuario();
        u.setId(3L);
        u.setUsername("admin");
        u.setRol("ADMIN");
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(u));
        when(usuarioActual.id()).thenReturn(3L);
        UsuarioRq rq = new UsuarioRq();
        rq.setId(3L);
        rq.setUsername("admin2");
        rq.setEmail("a@clinica.com");
        rq.setRol("ADMIN");
        assertThrows(ConflictException.class, () -> usuarios.actualizarUsuario(rq));
    }
}
