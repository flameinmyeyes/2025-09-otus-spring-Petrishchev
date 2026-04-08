package com.union.app.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {
    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Union Messenger API")
                        .version("1.0.0")
                        .description("""
                                API для мессенджера Union. Возможности:
                                - Аутентификация и регистрация (JWT)
                                - Управление профсоюзами (Union)
                                - Обмен сообщениями (групповые и приватные)
                                - Создание и участие в голосованиях (Polls)
                                - Управление событиями (Events)
                                - Управление друзьями
                                - Поиск пользователей
                                """)
                )
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .name("BearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Введите JWT токен в формате: Bearer {token}")));
    }
}