package com.faulttolerance.fidelity.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI fidelityOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Fidelity Service API")
                .description("Service for managing bonus points with asynchronous processing and fault tolerance")
                .version("1.0")
                .contact(new Contact()
                    .name("Development Team")
                    .email("dev@example.com")));
    }
}
