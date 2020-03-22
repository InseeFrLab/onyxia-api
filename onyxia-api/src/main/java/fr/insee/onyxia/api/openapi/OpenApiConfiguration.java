package fr.insee.onyxia.api.openapi;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@SecurityScheme(type = SecuritySchemeType.OPENIDCONNECT)
public class OpenApiConfiguration {

}
