package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.MedicoService;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.security.Roles;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Implementación de {@link UsuarioService}.
 */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Set<String> ROLES = Set.of(Roles.ADMIN, Roles.SECRETARIA, Roles.MEDICO, Roles.DESPACHO_MEDICO, "CLIENTE");
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9._-]{3,50}");
    private static final Pattern EMAIL = Pattern.compile("[^@\\s<>]+@[^@\\s<>]+\\.[^@\\s<>]+");

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioActual usuarioActual;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private MedicoService medicoService;

    @Override
    public List<Usuario> obtenerUsuarios() {
        return usuarioRepository.findAll();
    }

    @Override
    @Transactional
    public Usuario guardarUsuario(UsuarioRq rq) {
        if (rq == null || rq.getUsername() == null || !USERNAME.matcher(rq.getUsername().trim()).matches()) {
            throw new BadRequestException("El usuario debe tener entre 3 y 50 caracteres (letras, números, punto, guion)");
        }
        validarComunes(rq);
        validarPassword(rq.getPassword(), true);
        if (usuarioRepository.findByUsername(rq.getUsername().trim()).isPresent()) {
            throw new ConflictException("Ya existe el usuario '" + rq.getUsername().trim() + "'");
        }
        Usuario u = new Usuario();
        u.setUsername(rq.getUsername().trim());
        u.setEmail(rq.getEmail().trim());
        u.setRol(rq.getRol().trim().toUpperCase());
        u.setPasswordHash(passwordEncoder.encode(rq.getPassword()));
        u.setFechaCreacion(LocalDateTime.now());
        u.setActivo(true);
        u = usuarioRepository.save(u);
        if (Roles.MEDICO.equals(u.getRol())) {
            // Mismo proceso: si los datos profesionales no son válidos se revierte también el usuario.
            medicoService.guardarMedico(datosMedico(rq, u.getId()));
        }
        return u;
    }

    @Override
    @Transactional
    public Usuario actualizarUsuario(UsuarioRq rq) {
        if (rq == null || rq.getId() == null || rq.getId() <= 0) {
            throw new BadRequestException("El id del usuario es obligatorio");
        }
        Usuario u = usuarioRepository.findById(rq.getId())
                .orElseThrow(() -> new NotFoundException("El usuario " + rq.getId() + " no existe"));
        validarComunes(rq);
        if (rq.getUsername() != null && !rq.getUsername().trim().equals(u.getUsername())) {
            String nuevo = rq.getUsername().trim();
            if (!USERNAME.matcher(nuevo).matches()) {
                throw new BadRequestException("El usuario debe tener entre 3 y 50 caracteres (letras, números, punto, guion)");
            }
            if (u.getId().equals(usuarioActual.id())) {
                throw new ConflictException("No puede cambiar su propio nombre de usuario con la sesión abierta");
            }
            if (usuarioRepository.findByUsername(nuevo).isPresent()) {
                throw new ConflictException("Ya existe el usuario '" + nuevo + "'");
            }
            u.setUsername(nuevo);
        }
        String nuevoRol = rq.getRol().trim().toUpperCase();
        if (u.getId().equals(usuarioActual.id()) && !Roles.ADMIN.equals(nuevoRol)) {
            throw new ConflictException("No puede quitarse a sí mismo el rol de administrador");
        }
        if (rq.getPassword() != null && !rq.getPassword().isBlank()) {
            validarPassword(rq.getPassword(), false);
            u.setPasswordHash(passwordEncoder.encode(rq.getPassword()));
        }
        Medico medico = medicoRepository.findByUsuarioId(u.getId()).orElse(null);
        if (medico != null && !Roles.MEDICO.equals(nuevoRol)) {
            throw new ConflictException("Este usuario tiene un médico asociado: su rol debe seguir siendo MEDICO");
        }
        u.setEmail(rq.getEmail().trim());
        u.setRol(nuevoRol);
        u = usuarioRepository.save(u);
        if (Roles.MEDICO.equals(nuevoRol)) {
            MedicoRq datos = datosMedico(rq, u.getId());
            if (medico != null) {
                datos.setId(medico.getId());
                medicoService.actualizarMedico(datos);
            } else {
                medicoService.guardarMedico(datos);
            }
        }
        return u;
    }

    @Override
    @Transactional
    public Usuario cambiarEstado(Long id, boolean activo) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("El usuario " + id + " no existe"));
        if (!activo && u.getId().equals(usuarioActual.id())) {
            throw new ConflictException("No puede inactivar su propio usuario");
        }
        // El médico asociado sigue al usuario (se bloquea si tiene citas futuras pendientes).
        medicoRepository.findByUsuarioId(u.getId()).ifPresent(m -> medicoService.cambiarEstado(m.getId(), activo));
        u.setActivo(activo);
        return usuarioRepository.save(u);
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("El id de usuario es obligatorio");
        }
        if (id.equals(usuarioActual.id())) {
            throw new ConflictException("No puede eliminar su propio usuario");
        }
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("El usuario " + id + " no existe"));

        // Eliminar el médico asociado si existe (para no dejar huérfanos).
        medicoRepository.findByUsuarioId(id).ifPresent(m -> medicoRepository.delete(m));

        usuarioRepository.delete(u);
    }

    private MedicoRq datosMedico(UsuarioRq rq, Long usuarioId) {
        if (rq.getMedico() == null) {
            throw new BadRequestException("Los datos profesionales del médico son obligatorios para el rol MEDICO");
        }
        MedicoRq m = rq.getMedico();
        m.setId(null);
        m.setUsuarioId(usuarioId);
        return m;
    }

    private void validarComunes(UsuarioRq rq) {
        if (rq.getEmail() == null || !EMAIL.matcher(rq.getEmail().trim()).matches() || rq.getEmail().length() > 150) {
            throw new BadRequestException("El correo electrónico no es válido");
        }
        if (rq.getRol() == null || !ROLES.contains(rq.getRol().trim().toUpperCase())) {
            throw new BadRequestException("El rol debe ser uno de " + ROLES);
        }
    }

    private void validarPassword(String password, boolean obligatoria) {
        if (password == null || password.isBlank()) {
            if (obligatoria) {
                throw new BadRequestException("La contraseña es obligatoria");
            }
            return;
        }
        if (password.length() < 8 || password.length() > 72) {
            throw new BadRequestException("La contraseña debe tener entre 8 y 72 caracteres");
        }
    }
}
