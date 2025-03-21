package fr.insee.onyxia.api.security;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.utils.HttpRequestUtils;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.region.Region;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.SSLContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.client.RestTemplate;
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

    @Value("${oidc.roles-claim}")
    private String rolesClaim;

    @Value("${oidc.issuer-uri}")
    private String issuerUri;

    @Value("${oidc.public-key}")
    private String publicKey;

    @Value("${oidc.audience}")
    private String audience;

    @Value("${oidc.clientID}")
    private String clientID;

    @Value("${oidc.skip-tls-verify}")
    private boolean skipTlsVerify;

    @Value("${oidc.extra-query-params}")
    private String extraQueryParams;

    @Value("${oidc.scope}")
    private String scope;

    @Value("${oidc.idleSessionLifetimeInSeconds}")
    private Integer idleSessionLifetimeInSeconds;

    private final HttpRequestUtils httpRequestUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(OIDCConfiguration.class);

    @Autowired
    public OIDCConfiguration(HttpRequestUtils httpRequestUtils) {
        this.httpRequestUtils = httpRequestUtils;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        configurer -> {
                            configurer.sessionAuthenticationStrategy(
                                    sessionAuthenticationStrategy());
                            configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                        })

                // manage routes security here
                .authorizeHttpRequests(
                        configurer -> {
                            configurer.requestMatchers(HttpMethod.OPTIONS).permitAll();
                            // configuration pour Swagger
                            configurer.requestMatchers(antMatcher("/")).permitAll();
                            configurer.requestMatchers(antMatcher("/swagger-ui**")).permitAll();
                            configurer.requestMatchers(antMatcher("/swagger-ui/**")).permitAll();
                            configurer.requestMatchers(antMatcher("/v2/api-docs")).permitAll();
                            configurer.requestMatchers(antMatcher("/v3/api-docs")).permitAll();
                            configurer.requestMatchers(antMatcher("/v3/api-docs/*")).permitAll();
                            configurer.requestMatchers(antMatcher("/csrf")).permitAll();
                            configurer.requestMatchers(antMatcher("/webjars/**")).permitAll();
                            configurer
                                    .requestMatchers(antMatcher("/swagger-resources/**"))
                                    .permitAll();
                            configurer.requestMatchers(antMatcher("/actuator/**")).permitAll();
                            configurer.requestMatchers(antMatcher("/actuator")).permitAll();
                            configurer.requestMatchers(antMatcher("/api")).permitAll();
                            configurer.requestMatchers(antMatcher("/api/swagger-ui**")).permitAll();
                            configurer
                                    .requestMatchers(antMatcher("/api/swagger-ui/**"))
                                    .permitAll();
                            configurer.requestMatchers(antMatcher("/api/v2/api-docs")).permitAll();
                            configurer.requestMatchers(antMatcher("/api/v3/api-docs")).permitAll();
                            configurer
                                    .requestMatchers(antMatcher("/api/v3/api-docs/*"))
                                    .permitAll();
                            configurer.requestMatchers(antMatcher("/api/csrf")).permitAll();
                            configurer.requestMatchers(antMatcher("/api/webjars/**")).permitAll();
                            configurer
                                    .requestMatchers(antMatcher("/api/swagger-resources/**"))
                                    .permitAll();
                            configurer.requestMatchers(antMatcher("/api/actuator/**")).permitAll();
                            configurer.requestMatchers(antMatcher("/api/actuator")).permitAll();
                            configurer.requestMatchers(antMatcher("/configuration/**")).permitAll();
                            configurer
                                    .requestMatchers(antMatcher("/swagger-resources/**"))
                                    .permitAll();
                            // configuration pour public
                            configurer.requestMatchers(antMatcher("/public/**")).permitAll();
                            configurer.requestMatchers(antMatcher("/api/public/**")).permitAll();
                            configurer.anyRequest().authenticated();
                        })
                .oauth2ResourceServer(
                        httpSecurityOAuth2ResourceServerConfigurer -> {
                            httpSecurityOAuth2ResourceServerConfigurer.jwt(
                                    Customizer.withDefaults());
                        });
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
        return (Jwt) authentication.getPrincipal();
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
            if (userInfo.getClaimAsStringList(rolesClaim) != null) {
                List<String> roles = userInfo.getClaimAsStringList(rolesClaim);
                Collections.sort(roles);
                user.setRoles(roles);
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

    public String getExtraQueryParams() {
        return extraQueryParams;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setExtraQueryParams(String extraQueryParams) {
        this.extraQueryParams = extraQueryParams;
    }

    public boolean isSkipTlsVerify() {
        return skipTlsVerify;
    }

    public void setSkipTlsVerify(boolean skipTlsVerify) {
        this.skipTlsVerify = skipTlsVerify;
    }

    public String getRolesClaim() {
        return rolesClaim;
    }

    public void setRolesClaim(String rolesClaim) {
        this.rolesClaim = rolesClaim;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setIdleSessionLifetimeInSeconds(Integer idleSessionLifetimeInSeconds) {
        this.idleSessionLifetimeInSeconds = idleSessionLifetimeInSeconds;
    }

    public Integer getIdleSessionLifetimeInSeconds() {
        return idleSessionLifetimeInSeconds;
    }

    @Bean
    @ConditionalOnProperty(prefix = "oidc", name = "issuer-uri")
    NimbusJwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = null;
        OAuth2TokenValidator<Jwt> validator = JwtValidators.createDefault();
        if (StringUtils.isNotEmpty(publicKey)) {
            LOGGER.info("OIDC : using public key {} to validate tokens", publicKey);
            try {
                byte[] decodedKey = Base64.getDecoder().decode(publicKey);
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                RSAPublicKey parsedPublicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
                decoder = NimbusJwtDecoder.withPublicKey(parsedPublicKey).build();
            } catch (Exception e) {
                LOGGER.error(
                        "Fatal : Could not parse or use provided public key, please double check",
                        e);
                System.exit(0);
            }
        } else {
            LOGGER.info("OIDC : using issuerURI {} to validate tokens", issuerUri);
            if (skipTlsVerify) {
                try {
                    decoder =
                            NimbusJwtDecoder.withIssuerLocation(issuerUri)
                                    .restOperations(getRestTemplate())
                                    .build();
                } catch (Exception e) {
                    LOGGER.error("Fatal : failed to disable SSL verification", e);
                    System.exit(0);
                }
            } else {
                decoder = NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
            }
            validator = JwtValidators.createDefaultWithIssuer(issuerUri);
        }

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator();
        OAuth2TokenValidator<Jwt> withAudience =
                new DelegatingOAuth2TokenValidator<>(validator, audienceValidator);

        decoder.setJwtValidator(withAudience);

        return decoder;
    }

    public RestTemplate getRestTemplate()
            throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        HttpComponentsClientHttpRequestFactory requestFactoryHttp =
                new HttpComponentsClientHttpRequestFactory();

        TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
        SSLContext sslContext =
                SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        SSLConnectionSocketFactory sslsf =
                new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> socketFactoryRegistry =
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("https", sslsf)
                        .register("http", new PlainConnectionSocketFactory())
                        .build();

        BasicHttpClientConnectionManager connectionManager =
                new BasicHttpClientConnectionManager(socketFactoryRegistry);
        CloseableHttpClient httpClient =
                HttpClients.custom().setConnectionManager(connectionManager).build();
        requestFactoryHttp.setHttpClient(httpClient);
        return new RestTemplate(requestFactoryHttp);
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
