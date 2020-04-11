package fr.insee.onyxia.api.security;

import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.utils.HttpRequestUtils;
import fr.insee.onyxia.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@EnableWebSecurity
@ConditionalOnSingleCandidate(WebSecurityConfigurerAdapter.class)
public class NoSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private HttpRequestUtils httpRequestUtils;
   
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
      return () -> {
         User user = User.newInstance()
         .setEmail("toto@tld.fr")
         .setNomComplet("John doe")
         .setIdep("XXXXXX")
                 .setIp(httpRequestUtils.getClientIpAddressIfServletRequestExist(((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()))
                 .build();
         return user;
      };
   }

}
