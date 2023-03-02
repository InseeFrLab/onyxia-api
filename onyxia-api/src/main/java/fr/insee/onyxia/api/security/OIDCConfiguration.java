package fr.insee.onyxia.api.security;

import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.utils.HttpRequestUtils;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.region.Region;
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
import org.springframework.security.oauth2.jwt.Jwt;
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

    @Autowired
    private HttpRequestUtils httpRequestUtils;

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
            user.setIdep(userInfo.getClaims().get(usernameClaim).toString());
            user.setIp(
                    httpRequestUtils.getClientIpAddressIfServletRequestExist(
                            ((ServletRequestAttributes)
                                    RequestContextHolder
                                            .currentRequestAttributes())
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
}
