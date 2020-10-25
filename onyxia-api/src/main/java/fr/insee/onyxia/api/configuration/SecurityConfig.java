package fr.insee.onyxia.api.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    @Value("${security.strictmode}")
    private boolean strictmode;

    @Value("${security.cors.allowed_origins}")
    private String corsAllowedOrigins;

    public boolean isStrictmode() {
        return strictmode;
    }

    public void setStrictmode(boolean strictmode) {
        this.strictmode = strictmode;
    }

    public String getCorsAllowedOrigins() {
        return corsAllowedOrigins;
    }

    public void setCorsAllowedOrigins(String corsAllowedOrigins) {
        this.corsAllowedOrigins = corsAllowedOrigins;
    }
}
