package fr.insee.onyxia.api.security;

import fr.insee.onyxia.api.services.utils.HttpRequestUtils;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.management.HttpSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
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

   @Configuration
   @EnableWebSecurity
   @ConditionalOnProperty(name = "authentication.mode", havingValue = "openidconnect")
   @ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
   public class KeycloakConfigurationAdapter extends KeycloakWebSecurityConfigurerAdapter {

      @Configuration
      public static class KeycloakConfig {

         @Bean
         public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
            return new KeycloakSpringBootConfigResolver();
         }
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
