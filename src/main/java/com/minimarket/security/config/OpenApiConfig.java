package com.minimarket.security.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI minimarketOpenAPI() {
        final String schemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("MiniMarket Plus API")
                        .description("Documentación de los microservicios de MiniMarket Plus: " +
                                "gestión de productos, carrito, inventario, ventas y usuarios.")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Grupo 7 - Desarrollo Backend II")
                                .email("grupo7@duocuc.cl"))
                        .license(new License().name("Uso académico - Duoc UC")))
                // Habilita el botón "Authorize" en Swagger UI para probar con JWT
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components()
                        .addSecuritySchemes(schemeName, new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}