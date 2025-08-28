package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.uploads.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String fullPath = "file:" + System.getProperty("user.dir") + "/" + uploadPath + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(fullPath);
    }
}
