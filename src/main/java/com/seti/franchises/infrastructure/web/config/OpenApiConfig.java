package com.seti.franchises.infrastructure.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI (Swagger) para documentar la API de franquicias.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Franquicias")
                        .version("1.0")
                        .description("API REST reactiva para gestionar franquicias, sucursales y productos. " +
                                "Spring WebFlux + MongoDB + arquitectura hexagonal.")
                        .contact(new Contact()
                                .name("API Franquicias"))
                        .license(new License().name("Sin licencia")));
    }
}
