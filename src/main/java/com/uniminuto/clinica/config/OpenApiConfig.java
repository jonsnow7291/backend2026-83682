package com.uniminuto.clinica.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger/OpenAPI: información general, esquema Bearer JWT y grupos por módulo.
 * Interfaz disponible en {@code /clinica/v1/swagger-ui.html}.
 */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA = "bearerAuth";

    /**
     * @return definición OpenAPI con autenticación Bearer JWT global.
     */
    @Bean
    public OpenAPI clinicaOpenApi() {
        return new OpenAPI()
                .info(new Info().title("API Clínica Veterinaria")
                        .version("1.0")
                        .description("Inventario, citas, atención médica, facturación, despacho y reportes. "
                                + "Use POST /auth/login y pegue el token en 'Authorize'."))
                .components(new Components().addSecuritySchemes(ESQUEMA,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA));
    }

    /** @return grupo Seguridad y usuarios. */
    @Bean
    public GroupedOpenApi grupoSeguridad() {
        return GroupedOpenApi.builder().group("0-seguridad").displayName("Seguridad y usuarios")
                .pathsToMatch("/auth/**", "/usuario/**").build();
    }

    /** @return grupo Datos maestros. */
    @Bean
    public GroupedOpenApi grupoMaestros() {
        return GroupedOpenApi.builder().group("1-maestros").displayName("Clientes, mascotas y médicos")
                .pathsToMatch("/cliente/**", "/mascota/**", "/raza/**", "/medico/**", "/especializacion/**", "/tarifa/**")
                .build();
    }

    /** @return grupo Inventario. */
    @Bean
    public GroupedOpenApi grupoInventario() {
        return GroupedOpenApi.builder().group("2-inventario").displayName("Inventario y carga masiva")
                .pathsToMatch("/medicamento/**", "/inventario/**").build();
    }

    /** @return grupo Citas. */
    @Bean
    public GroupedOpenApi grupoCitas() {
        return GroupedOpenApi.builder().group("3-citas").displayName("Citas y horarios")
                .pathsToMatch("/cita/**", "/horario-veterinario/**").build();
    }

    /** @return grupo Atención y facturación. */
    @Bean
    public GroupedOpenApi grupoAtencion() {
        return GroupedOpenApi.builder().group("4-atencion").displayName("Atención, fórmulas y facturación")
                .pathsToMatch("/atencion/**", "/historia-medica/**", "/anotacion-historia/**",
                        "/formula-medica/**", "/factura/**", "/pago/**").build();
    }

    /** @return grupo Despacho. */
    @Bean
    public GroupedOpenApi grupoDespacho() {
        return GroupedOpenApi.builder().group("5-despacho").displayName("Despacho y dispensación")
                .pathsToMatch("/despacho/**").build();
    }

    /** @return grupo Reportes. */
    @Bean
    public GroupedOpenApi grupoReportes() {
        return GroupedOpenApi.builder().group("6-reportes").displayName("Reportes")
                .pathsToMatch("/reportes/**").build();
    }
}
