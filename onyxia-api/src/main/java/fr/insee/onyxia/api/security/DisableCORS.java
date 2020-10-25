package fr.insee.onyxia.api.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class DisableCORS implements WebMvcConfigurer {

    @Value("${security.cors.allowed_origins:#{null}}")
    private String corsAllowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (StringUtils.isNotEmpty(corsAllowedOrigins)) {
            registry.addMapping("/**").allowedOrigins(corsAllowedOrigins).allowedMethods("*");
        }
    }
}
