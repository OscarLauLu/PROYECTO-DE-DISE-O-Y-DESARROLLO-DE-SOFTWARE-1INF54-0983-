package com.paqrap.logistics.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI / Swagger.
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Sistema de Logística - API",
        version = "1.0",
        description = "Documentación de la API para el backend de gestión logística y de envíos de PaqRap."
    )
)
public class OpenApiConfig {
}
