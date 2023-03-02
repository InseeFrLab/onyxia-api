package fr.insee.onyxia.api.security;

import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.region.Region;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@Configuration
@ConditionalOnProperty(name = "authentication.mode", havingValue = "openidconnect")
public class OIDCConfiguration {

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

    @Bean
    public UserProvider getUserProvider() {
        return (Region region) -> {
            final User user = User.newInstance().build();
            return user;
        };
    }
}
