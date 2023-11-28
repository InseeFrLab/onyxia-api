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

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

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

    @Value("${oidc.clientID}")
    private String clientID;

    @Autowired private HttpRequestUtils httpRequestUtils;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf()
                .disable()
                .sessionManagement()
                // use previously declared bean
                .sessionAuthenticationStrategy(sessionAuthenticationStrategy())
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                .and()
                // manage routes securisation here
                .authorizeRequests()
                .requestMatchers(HttpMethod.OPTIONS)
                .permitAll()
                // configuration pour Swagger
            .requestMatchers(antMatcher("/")).permitAll()
            .requestMatchers(antMatcher("/swagger-ui**")).permitAll()
            .requestMatchers(antMatcher("/swagger-ui/**")).permitAll()
            .requestMatchers(antMatcher("/v2/api-docs")).permitAll()
            .requestMatchers(antMatcher("/v3/api-docs")).permitAll()
            .requestMatchers(antMatcher("/v3/api-docs/*")).permitAll()
            .requestMatchers(antMatcher("/csrf")).permitAll()
            .requestMatchers(antMatcher("/webjars/**")).permitAll()
            .requestMatchers(antMatcher("/swagger-resources/**")).permitAll()
            .requestMatchers(antMatcher("/actuator/**")).permitAll()
            .requestMatchers(antMatcher("/actuator")).permitAll()
            .requestMatchers(antMatcher("/api")).permitAll()
            .requestMatchers(antMatcher("/api/swagger-ui**")).permitAll()
            .requestMatchers(antMatcher("/api/swagger-ui/**")).permitAll()
            .requestMatchers(antMatcher("/api/v2/api-docs")).permitAll()
            .requestMatchers(antMatcher("/api/v3/api-docs")).permitAll()
            .requestMatchers(antMatcher("/api/v3/api-docs/*")).permitAll()
            .requestMatchers(antMatcher("/api/csrf")).permitAll()
            .requestMatchers(antMatcher("/api/webjars/**")).permitAll()
            .requestMatchers(antMatcher("/api/swagger-resources/**")).permitAll()
            .requestMatchers(antMatcher("/api/actuator/**")).permitAll()
            .requestMatchers(antMatcher("/api/actuator")).permitAll()
            .requestMatchers(antMatcher("/configuration/**")).permitAll()
            .requestMatchers(antMatcher("/swagger-resources/**")).permitAll()
            // configuration pour public
            .requestMatchers(antMatcher("/public/**"))
            .permitAll()
            .requestMatchers(antMatcher("/api/public/**"))
            .permitAll()
            .anyRequest()
                .authenticated()
              .and()
            .oauth2ResourceServer()
              .jwt()
        ;
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
            if (userInfo.getClaimAsStringList(groupsClaim) != null) {
                user.setGroups(userInfo.getClaimAsStringList(groupsClaim));
            }
            user.getAttributes().putAll(userInfo.getClaims());
            user.getAttributes().put("sub", userInfo.getSubject());
            user.getAttributes().put("access_token", userInfo.getTokenValue());
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

    public String getGroupsClaim() {
        return groupsClaim;
    }

    public void setGroupsClaim(String groupsClaim) {
        this.groupsClaim = groupsClaim;
    }

    public String getIssuerUri() {
        return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public String getJwkUri() {
        return jwkUri;
    }

    public void setJwkUri(String jwkUri) {
        this.jwkUri = jwkUri;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
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
