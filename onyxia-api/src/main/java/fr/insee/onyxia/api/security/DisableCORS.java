package fr.insee.onyxia.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(name = "cors.allowed", havingValue = "true")
public class DisableCORS implements WebMvcConfigurer {

    @Value("${cors.origins.allowed}")
    private String corsOriginsAllowed;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins(corsOriginsAllowed).allowedMethods("*");
    }
}
