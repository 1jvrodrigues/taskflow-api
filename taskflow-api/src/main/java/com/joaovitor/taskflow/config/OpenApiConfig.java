package com.joaovitor.taskflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskFlowOpenApi() {
        return new OpenAPI().info(new Info()
                .title("TaskFlow API")
                .description("API REST para gerenciamento de tarefas (CRUD, filtro por status e paginação).")
                .version("1.0.0"));
    }
}
