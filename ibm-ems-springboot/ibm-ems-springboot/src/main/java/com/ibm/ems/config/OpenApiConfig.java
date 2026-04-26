package com.ibm.ems.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${ems.app.name:IBM Employee Management System}")
    private String appName;

    @Value("${ems.app.version:1.0.0}")
    private String appVersion;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                // ── API Info ─────────────────────────────────────────────────
                .info(new Info()
                        .title(appName + " — REST API")
                        .version(appVersion)
                        .description(
                            "**IBM Employee Management System** REST API\n\n" +
                            "State Bank of India\n\n" +
                            "### Authentication\n" +
                            "1. Call `POST /api/v1/auth/login` with `hr.admin` / `Admin@IBM123`\n" +
                            "2. Copy the `token` from the response\n" +
                            "3. Click **Authorize** above and enter: `Bearer <your-token>`\n\n" +
                            "### Accounts\n" +
                            "| Username | Password | Role | Salary Access |\n" +
                            "|---|---|---|---|\n" +
                            "| `hr.admin` | `Admin@IBM123` | ADMIN | Full access |\n" +
                            "| `emp.user` | `User@IBM123` | USER | Salary masked |\n\n" +
                            
                            "⚠️ Swagger UI is **disabled in production** " +
                            "(springdoc.swagger-ui.enabled=false in application-prod.properties).\n" +
                            "Swagger is an attack surface — it documents every endpoint " +
                            "and parameter for potential attackers."
                        )
                        .contact(new Contact()
                                .name("IBM Technology Training Team")
                                .email("training@ibm.co.in"))
                        .license(new License()
                                .name("Internal Training Use Only")
                                .url("https://www.ibm.co.in")))

                // ── Servers ───────────────────────────────────────────────────
                .servers(List.of(
                        new Server()
                            .url("http://localhost:8080")
                            .description("Local Development")))

                // ── JWT Bearer Security Scheme ─────────────────────────────────
                // This registers the "Authorize" button in Swagger UI
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token (without 'Bearer ' prefix)")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
