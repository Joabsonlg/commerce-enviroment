package com.faulttolerance.store.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI storeOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Store Service API")
                .description("Service for managing products and processing sales in the fault-tolerant e-commerce system")
                .version("1.0")
                .contact(new Contact()
                    .name("Development Team")
                    .email("dev@example.com")));
    }
}
