package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.EspecializacionRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.security.Roles;
import com.uniminuto.clinica.service.MedicoService;
import com.uniminuto.clinica.util.CitaEstados;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Implementación de {@link MedicoService}.
 */
@Service
public class MedicoServiceImpl implements MedicoService {

    private static final Set<String> TIPOS_DOCUMENTO = Set.of("CC", "CE", "TI", "PA", "NIT");
    private static final Pattern DOCUMENTO = Pattern.compile("[A-Za-z0-9]{5,20}");
    private static final Pattern TELEFONO = Pattern.compile("[0-9+\\s-]{7,20}");
    private static final Pattern SIN_MARCAS = Pattern.compile("[^<>]*");

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private EspecializacionRepository especializacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<Medico> obtenerMedicos() {
        return medicoRepository.findAll();
    }

    @Override
    public Medico cambiarEstado(Long id, boolean activo) {
        Medico e = medicoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("El médico " + id + " no existe"));
        if (!activo && citaRepository.existsByMedico_IdAndEstadoInAndFechaHoraAfter(
                id, List.of(CitaEstados.PENDIENTE, CitaEstados.CONFIRMADA), LocalDateTime.now())) {
            throw new ConflictException("No se puede inactivar al médico: tiene citas pendientes o confirmadas a futuro. "
                    + "Cancele o reprograme esas citas primero.");
        }
        e.setActivo(activo);
        return medicoRepository.save(e);
    }

    @Override
    @Transactional
    public Medico guardarMedico(MedicoRq rq) {
        validar(rq, null);
        Medico m = new Medico();
        aplicar(m, rq);
        m.setActivo(true);

        // Si no se indica un usuario, se crea automáticamente una cuenta de acceso
        // con rol MEDICO para que el veterinario pueda iniciar sesión y gestionar sus citas.
        if (rq.getUsuarioId() == null) {
            String username = rq.getNumeroDocumento().trim();
            if (usuarioRepository.findByUsername(username).isPresent()) {
                throw new ConflictException("Ya existe un usuario con el documento " + username);
            }
            Usuario u = new Usuario();
            u.setUsername(username);
            u.setEmail(username + "@clinica.local");
            u.setRol(Roles.MEDICO);
            u.setPasswordHash(passwordEncoder.encode("Medico123!"));
            u.setFechaCreacion(LocalDateTime.now());
            u.setActivo(true);
            u = usuarioRepository.save(u);
            m.setUsuarioId(u.getId());
        }

        return medicoRepository.save(m);
    }

    @Override
    public Medico actualizarMedico(MedicoRq rq) {
        if (rq == null || rq.getId() == null || rq.getId() <= 0) {
            throw new BadRequestException("El id del médico es obligatorio");
        }
        Medico m = medicoRepository.findById(rq.getId())
                .orElseThrow(() -> new NotFoundException("El médico " + rq.getId() + " no existe"));
        validar(rq, m.getId());
        aplicar(m, rq);
        return medicoRepository.save(m);
    }

    private void validar(MedicoRq rq, Long idExcluir) {
        if (rq == null) {
            throw new BadRequestException("Los datos del médico son obligatorios");
        }
        String tipo = rq.getTipoDocumento() == null ? "" : rq.getTipoDocumento().trim().toUpperCase();
        if (!TIPOS_DOCUMENTO.contains(tipo)) {
            throw new BadRequestException("El tipo de documento debe ser uno de " + TIPOS_DOCUMENTO);
        }
        if (rq.getNumeroDocumento() == null || !DOCUMENTO.matcher(rq.getNumeroDocumento().trim()).matches()) {
            throw new BadRequestException("El número de documento debe tener entre 5 y 20 caracteres alfanuméricos");
        }
        texto(rq.getNombres(), "Los nombres", 100);
        texto(rq.getApellidos(), "Los apellidos", 100);
        texto(rq.getRegistroProfesional(), "El registro profesional", 50);
        if (rq.getTelefono() != null && !rq.getTelefono().isBlank()
                && !TELEFONO.matcher(rq.getTelefono().trim()).matches()) {
            throw new BadRequestException("El teléfono debe tener entre 7 y 20 dígitos");
        }
        if (rq.getEspecializacionId() == null || !especializacionRepository.existsById(rq.getEspecializacionId().longValue())) {
            throw new BadRequestException("La especialidad seleccionada no existe");
        }
        for (Medico otro : medicoRepository.findAll()) {
            if (otro.getId().equals(idExcluir)) {
                continue;
            }
            if (otro.getNumeroDocumento().equalsIgnoreCase(rq.getNumeroDocumento().trim())) {
                throw new ConflictException("Ya existe un médico con el documento " + rq.getNumeroDocumento().trim());
            }
            if (otro.getRegistroProfesional().equalsIgnoreCase(rq.getRegistroProfesional().trim())) {
                throw new ConflictException("Ya existe un médico con el registro profesional " + rq.getRegistroProfesional().trim());
            }
            if (rq.getUsuarioId() != null && rq.getUsuarioId().equals(otro.getUsuarioId())) {
                throw new ConflictException("Ese usuario ya está asociado a otro médico");
            }
        }
        if (rq.getUsuarioId() != null) {
            Usuario u = usuarioRepository.findById(rq.getUsuarioId())
                    .orElseThrow(() -> new BadRequestException("El usuario " + rq.getUsuarioId() + " no existe"));
            if (!Roles.MEDICO.equals(u.getRol())) {
                throw new BadRequestException("El usuario asociado debe tener el rol MEDICO");
            }
        }
    }

    private void texto(String valor, String campo, int max) {
        if (valor == null || valor.isBlank()) {
            throw new BadRequestException("Campo obligatorio: " + campo.toLowerCase());
        }
        if (valor.trim().length() > max) {
            throw new BadRequestException(campo + " no puede superar " + max + " caracteres");
        }
        if (!SIN_MARCAS.matcher(valor).matches()) {
            throw new BadRequestException(campo + " contiene caracteres no permitidos (< >)");
        }
    }

    private void aplicar(Medico m, MedicoRq rq) {
        m.setTipoDocumento(rq.getTipoDocumento().trim().toUpperCase());
        m.setNumeroDocumento(rq.getNumeroDocumento().trim());
        m.setNombres(rq.getNombres().trim());
        m.setApellidos(rq.getApellidos().trim());
        m.setTelefono(rq.getTelefono() == null || rq.getTelefono().isBlank() ? null : rq.getTelefono().trim());
        m.setRegistroProfesional(rq.getRegistroProfesional().trim());
        m.setEspecializacionId(rq.getEspecializacionId());
        m.setUsuarioId(rq.getUsuarioId());
    }

    @Override
    @Transactional
    public MiRespuestaRS crearAccesoUsuario(Long id) throws BadRequestException {
        if (id == null || id <= 0) {
            throw new BadRequestException("El id del médico es obligatorio");
        }
        Medico m = medicoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("El médico " + id + " no existe"));
        if (m.getUsuarioId() != null) {
            throw new ConflictException("El médico ya tiene una cuenta de acceso asociada");
        }
        String username = m.getNumeroDocumento().trim();
        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new ConflictException("Ya existe un usuario con el documento " + username);
        }
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setEmail(username + "@clinica.local");
        u.setRol(Roles.MEDICO);
        u.setPasswordHash(passwordEncoder.encode("Medico123!"));
        u.setFechaCreacion(LocalDateTime.now());
        u.setActivo(true);
        u = usuarioRepository.save(u);
        m.setUsuarioId(u.getId());
        medicoRepository.save(m);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Acceso de usuario creado para el médico " + m.getNombres() + " " + m.getApellidos()
                + ". Usuario: " + username + ", contraseña temporal: Medico123!");
        return respuesta;
    }

    @Override
    @Transactional
    public void eliminarMedico(Long id) throws BadRequestException {
        if (id == null || id <= 0) {
            throw new BadRequestException("El id del médico es obligatorio");
        }
        Medico m = medicoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("El médico " + id + " no existe"));

        // Si tiene usuario asociado, eliminarlo también para no dejar huérfanos.
        if (m.getUsuarioId() != null) {
            usuarioRepository.deleteById(m.getUsuarioId());
        }

        medicoRepository.delete(m);
    }
}
