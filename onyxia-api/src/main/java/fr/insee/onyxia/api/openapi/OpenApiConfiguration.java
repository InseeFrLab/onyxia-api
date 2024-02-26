package fr.insee.onyxia.api.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenApiConfiguration.class);

    @Value("${oidc.issuer-uri:#{null}}")
    private String issuerUri;

    @Bean
    @ConditionalOnSingleCandidate
    public OpenAPI customOpenAPI() {
        return baseOpenAPIConfiguration();
    }

    @Bean
    @ConditionalOnProperty(name = "authentication.mode", havingValue = "openidconnect")
    public OpenAPI createOpenAPI() {
        SecurityScheme scheme =
                new SecurityScheme()
                        .type(SecurityScheme.Type.OPENIDCONNECT)
                        .openIdConnectUrl(issuerUri + "/.well-known/openid-configuration");
        return baseOpenAPIConfiguration()
                .components(new Components().addSecuritySchemes("auth", scheme));
    }

    private OpenAPI baseOpenAPIConfiguration() {
        return new OpenAPI().info(new Info().title("Onyxia-api").description("Swagger onyxia-api"));
    }
}
