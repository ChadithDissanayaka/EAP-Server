package com.automobileproject.eap.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Prevents Spring's default static resource handler from intercepting
 * springdoc-openapi's /v3/api-docs and /swagger-ui paths.
 *
 * Without this, Spring Boot 3.4+ serves a 404 "No static resource v3/api-docs"
 * because the resource handler matches the path before DispatcherServlet
 * can route it to springdoc's controller.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Swagger UI static assets (webjars)
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/")
                .resourceChain(false);
    }
}
