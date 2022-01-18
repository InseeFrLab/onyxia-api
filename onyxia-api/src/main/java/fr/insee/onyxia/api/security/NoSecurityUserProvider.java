package fr.insee.onyxia.api.security;

import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.utils.HttpRequestUtils;
import fr.insee.onyxia.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class NoSecurityUserProvider {

    @Autowired
    private HttpRequestUtils httpRequestUtils;

    @Bean
    @Primary
    public UserProvider getUserProvider() {
        return () -> {
            User user = User.newInstance()
                    .setEmail("toto@tld.fr")
                    .setNomComplet("John doe")
                    .setIdep("default")
                    .setIp(httpRequestUtils.getClientIpAddressIfServletRequestExist(((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()))
                    .build();
            return user;
        };
    }
}