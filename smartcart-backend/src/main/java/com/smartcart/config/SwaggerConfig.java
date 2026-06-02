package com.smartcart.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger configuration for SmartCart.
 *
 * <p>Access the Swagger UI at: {@code /swagger-ui.html}
 * <p>API Docs JSON at: {@code /v3/api-docs}
 *
 * <p>APIs are grouped by module for easier navigation:
 * <ul>
 *   <li>Authentication — register, login</li>
 *   <li>Products — browse, search, filter</li>
 *   <li>Cart — add, update, remove items</li>
 *   <li>Orders — place, track, cancel orders</li>
 *   <li>Admin — dashboard, product/category/order management</li>
 * </ul>
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI smartCartOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("SmartCart E-Commerce API")
                        .description("Enterprise-grade REST API for the SmartCart E-Commerce Platform. "
                                + "Supports user authentication, product management, shopping cart, "
                                + "order processing, and payment integration with Razorpay.")
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("SmartCart Team")
                                .email("support@smartcart.com")
                                .url("https://github.com/smartcart"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .externalDocs(new ExternalDocumentation()
                        .description("SmartCart GitHub Repository")
                        .url("https://github.com/smartcart"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development"),
                        new Server().url("https://api.smartcart.com").description("Production")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token. Obtain it via POST /api/auth/login")));
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("1. Authentication")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi productApi() {
        return GroupedOpenApi.builder()
                .group("2. Products")
                .pathsToMatch("/api/products/**")
                .build();
    }

    @Bean
    public GroupedOpenApi cartApi() {
        return GroupedOpenApi.builder()
                .group("3. Cart")
                .pathsToMatch("/api/cart/**")
                .build();
    }

    @Bean
    public GroupedOpenApi orderApi() {
        return GroupedOpenApi.builder()
                .group("4. Orders")
                .pathsToMatch("/api/orders/**")
                .build();
    }

    @Bean
    public GroupedOpenApi paymentApi() {
        return GroupedOpenApi.builder()
                .group("5. Payments")
                .pathsToMatch("/api/payments/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("6. Admin")
                .pathsToMatch("/api/admin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("7. User Profile")
                .pathsToMatch("/api/user/**")
                .build();
    }
}
