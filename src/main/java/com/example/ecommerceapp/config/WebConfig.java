package com.example.ecommerceapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads");
        String uploadPath = uploadDir.toFile().getAbsolutePath().replace("\\", "/");

        // Assure le mapping file:/// pour Windows et Linux
        registry.addResourceHandler("/api/images/**", "/images/**")
                .addResourceLocations("file:///" + uploadPath + "/");

        System.out.println("🌐 Static resource handler configuré sur : file:///" + uploadPath + "/");
    }
}