package fr.insee.onyxia.api.security;

import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.utils.HttpRequestUtils;
import fr.insee.onyxia.model.User;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.management.HttpSessionManager;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.ArrayList;

public class SpringKeycloakSecurityConfiguration {
   
   @Configuration
   @EnableWebSecurity
   @ConditionalOnProperty(name = "authentication.mode", havingValue = "openidconnect")
   @ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
   public static class KeycloakConfigurationAdapter extends KeycloakWebSecurityConfigurerAdapter {

      @Autowired
      private HttpRequestUtils httpRequestUtils;
      
      @Bean
      @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
      public AccessToken getAccessToken() {
         HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
         KeycloakSecurityContext securityContext = (KeycloakSecurityContext) httpRequest.getAttribute(KeycloakSecurityContext.class.getName());
         return securityContext.getToken();
      }
      
      @Bean
      public UserProvider getUserProvider() {
         return () -> {
            AccessToken token = getAccessToken();
            List<String> groups = (List<String>) token.getOtherClaims().get("groups");
            if (groups == null) {
               groups = new ArrayList<String>();
            }
            User user = User.newInstance()
                    .addGroups(groups)
            .setEmail(token.getEmail())
            .setNomComplet(token.getName())
            .setIdep(token.getPreferredUsername())
                    .setIp(httpRequestUtils.getClientIpAddressIfServletRequestExist(((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()))
                    .build();
            return user;
         };
      }
      
      
      
      // permet de gérer l'erreur de doublon du bean httpSessionManager 
      @Bean
      @Override
      @ConditionalOnMissingBean(HttpSessionManager.class)
      protected HttpSessionManager httpSessionManager() {   
         return new HttpSessionManager();    
      }
      
      @Bean
      @Override
      protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
         // required for bearer-only applications.
         return new NullAuthenticatedSessionStrategy();
      }
      
      
      @Autowired
      public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
         KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
         // simple Authority Mapper to avoid ROLE_
         keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
         auth.authenticationProvider(keycloakAuthenticationProvider);
      }
      
      @Bean
      public KeycloakConfigResolver KeycloakConfigResolver() {
         return new KeycloakSpringBootConfigResolver();
      }
      
      @Override
      protected void configure(HttpSecurity http) throws Exception {
         http
         // disable csrf because of API mode
         .csrf().disable().sessionManagement()
         // use previously declared bean
         .sessionAuthenticationStrategy(sessionAuthenticationStrategy()).sessionCreationPolicy(SessionCreationPolicy.STATELESS)
         // keycloak filters for securisation
         .and().addFilterBefore(keycloakPreAuthActionsFilter(), LogoutFilter.class)
         .addFilterBefore(keycloakAuthenticationProcessingFilter(), X509AuthenticationFilter.class).exceptionHandling()
         .authenticationEntryPoint(authenticationEntryPoint()).and()
         // manage routes securisation here
         .authorizeRequests().antMatchers(HttpMethod.OPTIONS).permitAll()
         // configuration pour Swagger
         .antMatchers("/","/swagger-ui**","/swagger-ui/**", "/v2/api-docs","/v3/api-docs","/v3/api-docs/*","/csrf",  "/webjars/**", "/swagger-resources/**", "/actuator/**", "/actuator").permitAll()
         // configuration pour public
         .antMatchers("/public/**").permitAll()
         .anyRequest().authenticated()
         ;
      }
      
   }
}
