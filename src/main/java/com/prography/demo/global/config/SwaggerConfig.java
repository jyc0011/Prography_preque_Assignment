package com.prography.demo.global.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.*;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info apiInfo = new Info()
                .version("v1.0.0")
                .title("Prography Pre Assignment API Docs")
                .description("Ping-Pong Game API Docs Sheet");

        return new OpenAPI()
                .info(apiInfo);
    }
}
