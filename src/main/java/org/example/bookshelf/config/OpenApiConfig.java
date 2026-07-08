package org.example.bookshelf.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookshelfOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bookshelf API")
                        .description("REST API for managing a personal bookshelf")
                        .version("v1"));
    }
}
