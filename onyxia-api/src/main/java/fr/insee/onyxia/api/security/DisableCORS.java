package fr.insee.onyxia.api.security;

import fr.insee.onyxia.api.configuration.SecurityConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class DisableCORS implements WebMvcConfigurer {

    @Autowired
    private SecurityConfig securityConfig;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (StringUtils.isNotEmpty(securityConfig.getCorsAllowedOrigins())) {
            registry.addMapping("/**").allowedOrigins(securityConfig.getCorsAllowedOrigins()).allowedMethods("*");
        }
    }
}
