package com.faulttolerance.exchange.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI exchangeOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Exchange Service API")
                .description("Service for currency exchange rates with fault tolerance mechanisms")
                .version("1.0")
                .contact(new Contact()
                    .name("Development Team")
                    .email("dev@example.com")));
    }
}
