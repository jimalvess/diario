package com.diario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class DiarioApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(DiarioApplication.class, args);
    }

    // Método adicionado para configurar a aplicação quando implantada como WAR
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(DiarioApplication.class);
    }
}
