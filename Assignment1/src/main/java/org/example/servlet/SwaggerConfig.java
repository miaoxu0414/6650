package org.example.servlet;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${swagger.server.url:http://localhost:8080}") // 默认值为本地地址
    private String serverUrl;

    @Value("${swagger.server.description:Local Server}") // 默认描述为本地服务器
    private String serverDescription;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ski Data API for NEU Seattle distributed systems course")
                        .version("2.0")
                        .description("An API for an emulation of skier management system for RFID tagged lift tickets. "))
                .servers(List.of(
                        new Server().url(serverUrl).description(serverDescription)
                ));
    }
}