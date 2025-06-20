package com.diario.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Config de CORS para permitir requisições do frontend:
        registry.addMapping("/**")                                // Permite todas as rotas
                .allowedOrigins("http://localhost:5173")                    // URL do front
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // Métodos permitidos
                .allowedHeaders("*")                                        // Permite todos os cabeçalhos
                .allowCredentials(true);                                    // Permite o envio de cookies, se necessário
    }
}
