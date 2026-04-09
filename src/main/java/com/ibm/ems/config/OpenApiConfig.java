package com.ibm.ems.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
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
                .info(new Info()
                        .title(appName + " — REST API")
                        .version(appVersion)
                        .description("Complete REST API for managing employees and departments.\n\n" +
                                "**H2 Console:** [http://localhost:8090/h2-console](http://localhost:8090/h2-console)\n\n" +
                                "JDBC URL: `jdbc:h2:mem:emsdb` | User: `sa` | Password: *(empty)*")
                        .contact(new Contact()
                                .name("IBM Training Team")
                                .email("training@ibm.com"))
                        .license(new License()
                                .name("Internal Training Use")
                                .url("https://www.ibm.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8090")
                                    .description("Local Development Server")));
    }
}
