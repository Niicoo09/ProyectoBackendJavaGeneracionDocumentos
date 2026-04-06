package com.proyecto.document_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración personalizada de la documentación de la API (Swagger).
 * Aquí definimos el título, la versión y los datos de contacto del autor.
 * 
 * @author Nicolas Navarro Contreras
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Generación de Documentos")
                        .version("1.0.0")
                        .description("Servicio técnico para la creación automática de PDFs usando Playwright y Thymeleaf. Conexión directa con la base de datos de Coolify.")
                        .contact(new Contact()
                                .name("Nicolas Navarro Contreras")
                                .url("https://github.com/Niicoo09")));
    }
}
