package fr.insee.onyxia.api.security;

import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.utils.HttpRequestUtils;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.region.Region;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@ConditionalOnExpression("'${authentication.mode}' == 'none' or '${authentication.mode}' == ''")
public class NoSecurityUserProvider {

    @Autowired
    private HttpRequestUtils httpRequestUtils;

    @Bean
    public UserProvider getUserProvider() {
        return (Region region) -> User.newInstance()
                .setEmail("toto@tld.fr")
                .setNomComplet("John doe")
                .setIdep("default")
                .setIp(httpRequestUtils.getClientIpAddressIfServletRequestExist(
                        ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()))
                .build();
    }
}