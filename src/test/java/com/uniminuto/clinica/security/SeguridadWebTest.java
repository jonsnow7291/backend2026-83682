package com.uniminuto.clinica.security;

import com.uniminuto.clinica.apicontroller.AuthApiController;
import com.uniminuto.clinica.config.SecurityConfig;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.serviceimpl.AuthServiceImpl;
import com.uniminuto.clinica.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de la capa de seguridad: login, token, 401 y restricciones por rol (403).
 */
@WebMvcTest(controllers = AuthApiController.class, properties = {
        "app.jwt.secret=clave-de-prueba-con-mas-de-32-caracteres-123"})
@Import({SecurityConfig.class, JwtService.class, UsuarioActual.class, AuthServiceImpl.class, GlobalExceptionHandler.class})
class SeguridadWebTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PasswordEncoder encoder;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private MedicoRepository medicoRepository;

    private String loginComo(String username, String rol) throws Exception {
        Usuario u = new Usuario();
        u.setId(9L);
        u.setUsername(username);
        u.setRol(rol);
        u.setActivo(true);
        u.setPasswordHash(encoder.encode("Clinica2026*"));
        when(usuarioRepository.findByUsername(username)).thenReturn(Optional.of(u));
        when(medicoRepository.findByUsuarioId(9L)).thenReturn(Optional.empty());
        String json = mvc.perform(post("/auth/login").contentType("application/json")
                        .content("{\"username\":\"" + username + "\",\"password\":\"Clinica2026*\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    @Test
    void loginConClaveIncorrectaDevuelve401() throws Exception {
        when(usuarioRepository.findByUsername("x")).thenReturn(Optional.empty());
        mvc.perform(post("/auth/login").contentType("application/json")
                        .content("{\"username\":\"x\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginSinCamposDevuelve400() throws Exception {
        mvc.perform(post("/auth/login").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        mvc.perform(get("/auth/me")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void conTokenValidoDevuelveSesion() throws Exception {
        String token = loginComo("admin", Roles.ADMIN);
        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    void despachoMedicoRecibe403EnCitasYUsuarios() throws Exception {
        String token = loginComo("despacho", Roles.DESPACHO_MEDICO);
        mvc.perform(get("/cita/listar-todos").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(get("/usuario/listar-todos").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void medicoRecibe403EnDespachoYUsuarios() throws Exception {
        String token = loginComo("medico1", Roles.MEDICO);
        mvc.perform(get("/despacho/pendientes").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(get("/usuario/listar-todos").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
