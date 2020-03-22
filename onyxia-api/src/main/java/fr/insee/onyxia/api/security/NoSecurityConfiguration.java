package fr.insee.onyxia.api.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.model.User;

@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "keycloak.enabled", havingValue = "false")
public class NoSecurityConfiguration extends WebSecurityConfigurerAdapter {
   
   @Override
   protected void configure(HttpSecurity http) throws Exception {
       http.authorizeRequests().antMatchers("/**").permitAll();
       http.headers().frameOptions().disable();
       http.csrf().disable();

   }

   @Override
   public void configure(WebSecurity web) {
       web.ignoring().antMatchers("/**");
   }

   @Bean
   public UserProvider getUserProvider() {
      return new UserProvider() {
         
         @Override
         public User getUser() {
            User user = User.newInstance()
            .setEmail("toto@tld.fr")
            .setNomComplet("John doe")
            .setIdep("XXXXXX").build();
            return user;
         }
      };
   }

}
