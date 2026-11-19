package com.uniminuto.clinica.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.uniminuto.clinica.security.Roles;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Seguridad de la API: autenticación stateless con JWT (HS256) y autorización por rol validada
 * en el backend. Matriz base por URL; los endpoints sensibles pueden afinarla con
 * {@code @PreAuthorize} (habilitado con {@link EnableMethodSecurity}).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLICAS = {
            "/auth/login", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error"
    };

    /**
     * Cadena de filtros: rutas públicas, matriz de roles y respuestas JSON para 401/403.
     *
     * @param http configuración HTTP de Spring Security.
     * @return cadena de filtros.
     * @throws Exception si falla la construcción.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PUBLICAS).permitAll()
                        // Despacho: exclusivo del perfil DESPACHO_MEDICO
                        .requestMatchers("/despacho/**").hasRole(Roles.DESPACHO_MEDICO)
                        // Inventario: lectura para todos los perfiles, escritura para admin/secretaria
                        .requestMatchers(HttpMethod.GET, "/medicamento/**", "/inventario/**")
                        .hasAnyRole(Roles.ADMIN, Roles.SECRETARIA, Roles.MEDICO, Roles.DESPACHO_MEDICO)
                        .requestMatchers("/medicamento/**", "/inventario/**")
                        .hasAnyRole(Roles.ADMIN, Roles.SECRETARIA)
                        // Tarifas: lectura clínica, escritura solo ADMIN
                        .requestMatchers(HttpMethod.GET, "/tarifa/**").hasAnyRole(Roles.ADMIN, Roles.SECRETARIA, Roles.MEDICO)
                        .requestMatchers("/tarifa/**").hasRole(Roles.ADMIN)
                        // Estado de especialidades y médicos: ADMIN
                        .requestMatchers(HttpMethod.PATCH, "/especializacion/**", "/medico/**").hasRole(Roles.ADMIN)
                        // Horarios: lectura clínica, escritura admin/secretaria
                        .requestMatchers(HttpMethod.GET, "/horario-veterinario/**").hasAnyRole(Roles.ADMIN, Roles.SECRETARIA, Roles.MEDICO)
                        .requestMatchers("/horario-veterinario/**").hasAnyRole(Roles.ADMIN, Roles.SECRETARIA)
                        // Atención médica: la realiza el médico; secretaria solo consulta (prefactura)
                        .requestMatchers(HttpMethod.GET, "/atencion/**").hasAnyRole(Roles.ADMIN, Roles.SECRETARIA, Roles.MEDICO)
                        .requestMatchers("/atencion/**").hasAnyRole(Roles.ADMIN, Roles.MEDICO)
                        // Facturación y pagos
                        .requestMatchers("/factura/**", "/pago/**").hasAnyRole(Roles.ADMIN, Roles.SECRETARIA)
                        // Sesión propia: cualquier usuario autenticado
                        .requestMatchers("/auth/me").authenticated()
                        // Administración
                        .requestMatchers("/usuario/**", "/reportes/**").hasRole(Roles.ADMIN)
                        // Resto del sistema clínico: todo perfil excepto DESPACHO_MEDICO
                        .anyRequest().hasAnyRole(Roles.ADMIN, Roles.SECRETARIA, Roles.MEDICO))
                .oauth2ResourceServer(o -> o
                        .jwt(j -> j.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint((req, res, ex) ->
                                escribir(res, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED",
                                        "Debe iniciar sesión con un token válido")))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                escribir(res, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED",
                                        "Debe iniciar sesión con un token válido"))
                        .accessDeniedHandler((req, res, ex) ->
                                escribir(res, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN",
                                        "Su perfil no tiene permiso para esta operación")));
        return http.build();
    }

    /**
     * Convierte el claim {@code rol} del JWT en la autoridad {@code ROLE_<rol>}.
     *
     * @return conversor de autenticación JWT.
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String rol = jwt.getClaimAsString("rol");
            return rol == null ? List.<GrantedAuthority>of()
                    : List.<GrantedAuthority>of(new SimpleGrantedAuthority("ROLE_" + rol));
        });
        return converter;
    }

    /**
     * Escribe una respuesta de error JSON con el mismo formato de {@code ErrorResponse}.
     * Los mensajes son constantes, por lo que no requieren escape.
     */
    private static void escribir(HttpServletResponse res, int status, String error, String mensaje) throws java.io.IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.getWriter().write("{\"status\":" + status + ",\"error\":\"" + error + "\",\"message\":\"" + mensaje + "\"}");
    }

    /**
     * Codificador de contraseñas BCrypt.
     *
     * @return codificador.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Clave HMAC para firmar/verificar los tokens.
     *
     * @param secreto valor de {@code app.jwt.secret} (mínimo 32 caracteres).
     * @return clave secreta.
     */
    @Bean
    public SecretKey jwtKey(@Value("${app.jwt.secret}") String secreto) {
        return new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    /**
     * @param key clave de firma.
     * @return codificador de JWT.
     */
    @Bean
    public JwtEncoder jwtEncoder(SecretKey key) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    /**
     * @param key clave de firma.
     * @return decodificador de JWT que verifica firma y expiración.
     */
    @Bean
    public JwtDecoder jwtDecoder(SecretKey key) {
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    /**
     * CORS abierto para el frontend (los controladores ya declaran {@code @CrossOrigin}).
     *
     * @return configuración CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("*"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Content-Disposition"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
