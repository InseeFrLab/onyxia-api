package fr.insee.onyxia.api.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfiguration {


    private static final Logger logger = LoggerFactory.getLogger(OpenApiConfiguration.class);

    @Value("${keycloak.auth-server-url}")
    public String keycloakUrl;

    @Value("${keycloak.realm}")
    public String realmName;

    public final String SCHEMEKEYCLOAK = "oAuthScheme";

    @Bean
    @ConditionalOnProperty(name = "com.example.demo.auth.type", havingValue = "openidconnect", matchIfMissing = true)
    public OpenAPI customOpenAPIKeycloak() {
        final OpenAPI openapi = createOpenAPI();
        openapi.components(new Components().addSecuritySchemes(SCHEMEKEYCLOAK, new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2).in(SecurityScheme.In.HEADER).description("Authentification keycloak")
                .flows(new OAuthFlows().authorizationCode(new OAuthFlow()
                        .authorizationUrl(keycloakUrl + "/realms/" + realmName + "/protocol/openid-connect/auth")
                        .tokenUrl(keycloakUrl + "/realms/" + realmName + "/protocol/openid-connect/token")
                .refreshUrl(keycloakUrl + "/realms/" + realmName + "/protocol/openid-connect/token")))));
        return openapi;
    }

    @Bean
    @ConditionalOnProperty(name = "authentication.mode", havingValue = "none")
    public OpenAPI customOpenAPI() {
        final OpenAPI openapi = createOpenAPI();
        return openapi;
    }

    private OpenAPI createOpenAPI() {
        logger.info("surcharge de la configuration swagger");
        final OpenAPI openapi = new OpenAPI()
                .info(new Info().title("Onyxia-api").description("Swagger onyxia-api"));
        return openapi;
    }


    @ConditionalOnProperty(name = "authentication.mode", havingValue = "openidconnect", matchIfMissing = true)
    @Bean
    public OperationCustomizer addKeycloak() {
        return (operation, handlerMethod) -> {
            // all urls with /public doesn't need header authorization
            if (handlerMethod.getMethod().getName().startsWith("/public/")) {
                return operation;
            }
            return operation.addSecurityItem(new SecurityRequirement().addList(SCHEMEKEYCLOAK));
        };
    }
}
