package com.example.hotelproject.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hotel Booking System API")
                        .description("""
                                Hotel Booking System - Modular Monolith
                                
                                This API provides a complete hotel booking system including:
                                * Catalog Management - Hotels and Room Types
                                * Availability & Pricing - Check availability and calculate prices
                                * Booking Management - Create, confirm, and cancel bookings
                                * Payment Processing - Mock payment integration
                                * Notifications - Email notifications
                                * Authentication & Authorization - JWT based security
                                
                                ## Authentication
                                All endpoints except `/auth/login` and `/auth/register` require a JWT token.
                                Include the token in the Authorization header: `Bearer {token}`
                                
                                ## Base URL
                                `http://localhost:8080/api`
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Hotel Booking Team")
                                .email("team@hotelbooking.com")
                                .url("https://hotelbooking.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .servers(List.of(
                        new Server().url("http://localhost:8080/api").description("Local Development Server"),
                        new Server().url("https://staging.hotelbooking.com/api").description("Staging Server"),
                        new Server().url("https://api.hotelbooking.com").description("Production Server")
                ))
                .tags(List.of(
                        new Tag().name("01. Authentication").description("User registration, login, and profile management"),
                        new Tag().name("02. Hotel Catalog").description("Hotel management CRUD operations"),
                        new Tag().name("03. Room Types").description("Room type management for hotels"),
                        new Tag().name("04. Availability & Pricing").description("Check room availability and calculate prices"),
                        new Tag().name("05. Booking Management").description("Create, confirm, cancel, and search bookings"),
                        new Tag().name("06. Payment Processing").description("Mock payment processing and refunds"),
                        new Tag().name("07. Notifications").description("Send and manage email notifications")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token. Example: eyJhbGciOiJIUzI1NiIs...")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}