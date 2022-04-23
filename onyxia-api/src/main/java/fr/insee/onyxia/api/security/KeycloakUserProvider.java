package fr.insee.onyxia.api.security;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.utils.HttpRequestUtils;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.region.Region;

@Configuration
@ConditionalOnProperty(name = "authentication.mode", havingValue = "openidconnect")
public class KeycloakUserProvider {

    @Autowired
    private HttpRequestUtils httpRequestUtils;

    @Bean
    @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public AccessToken getAccessToken() {
	final HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
	final KeycloakSecurityContext securityContext = (KeycloakSecurityContext) httpRequest.getAttribute(KeycloakSecurityContext.class.getName());
	return securityContext.getToken();
    }

    @Bean
    @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.NO)
    public String getAccessTokenString() {
	final HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
	final KeycloakSecurityContext securityContext = (KeycloakSecurityContext) httpRequest.getAttribute(KeycloakSecurityContext.class.getName());
	return securityContext.getTokenString();
    }

    @Bean
    public UserProvider getUserProvider() {
	return (Region region) -> {
	    final AccessToken token = getAccessToken();
	    final String tokenString = getAccessTokenString();
        List<String> groups = ((List<?>) token.getOtherClaims().getOrDefault("groups", List.of()))
                .stream().map(String.class::cast)
                .collect(Collectors.toList());
        if (region.getIncludedGroupPattern() != null) {
            Pattern includePattern = Pattern.compile(region.getIncludedGroupPattern().toUpperCase());
            groups.removeIf(group -> !includePattern.matcher(group.toUpperCase()).matches());
        }
        if (region.getExcludedGroupPattern() != null) {
            Pattern excludePattern = Pattern.compile(region.getExcludedGroupPattern().toUpperCase());
            groups.removeIf(group -> excludePattern.matcher(group.toUpperCase()).matches());
        }
	    final User user = User.newInstance()
		    .addGroups(groups)
		    .setEmail(token.getEmail())
		    .setNomComplet(token.getName())
		    .setIdep(token.getPreferredUsername())
		    .setIp(httpRequestUtils.getClientIpAddressIfServletRequestExist(((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()))
		    .build();
	    user.getAttributes().putAll(token.getOtherClaims());
	    user.getAttributes().put("sub",token.getSubject());
	    user.getAttributes().put("preferred_username", token.getPreferredUsername());
	    user.getAttributes().put("access_token",tokenString);
	    return user;
	};
    }
}
