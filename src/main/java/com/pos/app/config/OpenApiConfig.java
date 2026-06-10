package com.pos.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "Bearer Authentication";

    @Bean
    public OpenAPI posOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("POS API")
                        .description("""
                                Point of Sale REST API.
                                Supports products, cart, orders, Chapa payments (Telebirr, CBE Birr, Bank Card, Cash), and refunds.
                                Login first, then use **Authorize** with: Bearer {your-jwt-token}
                                """)
                        .version("1.0.0")
                        .contact(new Contact().name("POS Team")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token returned from POST /api/auth/login")));
    }
}
