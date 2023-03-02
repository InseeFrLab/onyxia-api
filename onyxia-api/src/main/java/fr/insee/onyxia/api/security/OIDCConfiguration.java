package fr.insee.onyxia.api.security;

import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.utils.HttpRequestUtils;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.region.Region;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@ConditionalOnProperty(name = "authentication.mode", havingValue = "openidconnect")
public class OIDCConfiguration {

    @Value("${oidc.username-claim}")
    private String usernameClaim;

    @Value("${oidc.groups-claim}")
    private String groupsClaim;

    @Value("${oidc.issuer-uri}")
    private String issuerUri;

    @Value("${oidc.jwk-uri}")
    private String jwkUri;

    @Value("${oidc.audience}")
    private String audience;

    @Autowired private HttpRequestUtils httpRequestUtils;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf()
                .disable()
                .sessionManagement()
                // use previously declared bean
                .sessionAuthenticationStrategy(sessionAuthenticationStrategy())
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // manage routes securisation here
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS)
                .permitAll()
                // configuration pour Swagger
                .antMatchers(
                        "/",
                        "/swagger-ui**",
                        "/swagger-ui/**",
                        "/v2/api-docs",
                        "/v3/api-docs",
                        "/v3/api-docs/*",
                        "/csrf",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/actuator/**",
                        "/actuator")
                .permitAll()
                .antMatchers(
                        "/api",
                        "/api/swagger-ui**",
                        "/api/swagger-ui/**",
                        "/api/v2/api-docs",
                        "/api/v3/api-docs",
                        "/api/v3/api-docs/*",
                        "/api/csrf",
                        "/api/webjars/**",
                        "/api/swagger-resources/**",
                        "/api/actuator/**",
                        "/api/actuator")
                .permitAll()
                // configuration pour public
                .antMatchers("/public/**")
                .permitAll()
                .antMatchers("/api/public/**")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .oauth2ResourceServer()
                .jwt();
        return http.build();
    }

    @Bean
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        // required for bearer-only applications.
        return new NullAuthenticatedSessionStrategy();
    }

    @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.NO)
    public Jwt getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt;
    }

    @Bean
    public UserProvider getUserProvider() {
        return (Region region) -> {
            final User user = User.newInstance().build();
            Jwt userInfo = getUserInfo();
            user.setIdep(userInfo.getClaim(usernameClaim));
            user.setIp(
                    httpRequestUtils.getClientIpAddressIfServletRequestExist(
                            ((ServletRequestAttributes)
                                            RequestContextHolder.currentRequestAttributes())
                                    .getRequest()));
            user.setEmail(userInfo.getClaimAsString("email"));
            user.setNomComplet(userInfo.getClaimAsString("name"));
            user.setGroups(userInfo.getClaimAsStringList(groupsClaim));
            return user;
        };
    }

    public String getUsernameClaim() {
        return usernameClaim;
    }

    public void setUsernameClaim(String usernameClaim) {
        this.usernameClaim = usernameClaim;
    }

    public void setHttpRequestUtils(HttpRequestUtils httpRequestUtils) {
        this.httpRequestUtils = httpRequestUtils;
    }

    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public void setJwkUri(String jwkUri) {
        this.jwkUri = jwkUri;
    }

    @Bean
    @ConditionalOnProperty(prefix = "oidc", name = "issuer-uri")
    NimbusJwtDecoder jwtDecoder() {
        // If JWK is defined, use that instead of JWT issuer / audience validation
        if (StringUtils.isNotEmpty(jwkUri)) {
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkUri).build();
            return decoder;
        }

        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator();
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);

        OAuth2TokenValidator<Jwt> withAudience =
                new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);

        return jwtDecoder;
    }

    public class AudienceValidator implements OAuth2TokenValidator<Jwt> {
        OAuth2Error error =
                new OAuth2Error(
                        "invalid_token", "The required audience " + audience + " is missing", null);

        public OAuth2TokenValidatorResult validate(Jwt jwt) {
            if (StringUtils.isEmpty(audience) || jwt.getAudience().contains(audience)) {
                return OAuth2TokenValidatorResult.success();
            } else {
                return OAuth2TokenValidatorResult.failure(error);
            }
        }
    }
}
