package fr.insee.onyxia.api.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenApiConfiguration.class);

    // TODO : add oauth authentication

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI customOpenAPI() {
        return createOpenAPI();
    }

    private OpenAPI createOpenAPI() {
        LOGGER.info("surcharge de la configuration swagger");
        return new OpenAPI().info(new Info().title("Onyxia-api").description("Swagger onyxia-api"));
    }
}
