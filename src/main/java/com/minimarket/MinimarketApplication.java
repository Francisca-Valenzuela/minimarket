package com.minimarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@SpringBootApplication 
@SecurityScheme(
    name = "bearerAuth",             // Cambiado de 'nombre' a 'name'
    type = SecuritySchemeType.HTTP,  // Cambiado de 'tipo' a 'type'
    bearerFormat = "JWT", 
    scheme = "bearer"
) 
public class MinimarketApplication {
    public static void main(String[] args) {
        SpringApplication.run(MinimarketApplication.class, args); 
    }
}
