package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.uploads.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Utilisation de Paths.get pour éviter les séparateurs codés en dur
        String fullPath = Paths.get(System.getProperty("user.dir"), uploadPath).toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(fullPath);
    }
}
