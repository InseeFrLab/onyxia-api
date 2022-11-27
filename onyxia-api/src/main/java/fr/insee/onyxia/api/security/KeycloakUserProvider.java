package fr.insee.onyxia.api.security;

import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.utils.HttpRequestUtils;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.region.Region;
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

@Configuration
@ConditionalOnProperty(name = "authentication.mode", havingValue = "openidconnect")
public class KeycloakUserProvider {

    @Autowired private HttpRequestUtils httpRequestUtils;

    Pattern rfc1123Pattern = Pattern.compile("[a-z0-9]([-a-z0-9]*[a-z0-9])?");

    @Bean
    @Scope(
            scopeName = WebApplicationContext.SCOPE_REQUEST,
            proxyMode = ScopedProxyMode.TARGET_CLASS)
    public AccessToken getAccessToken() {
        final HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                        .getRequest();
        final KeycloakSecurityContext securityContext =
                (KeycloakSecurityContext)
                        httpRequest.getAttribute(KeycloakSecurityContext.class.getName());
        return securityContext.getToken();
    }

    @Bean
    @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.NO)
    public String getAccessTokenString() {
        final HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                        .getRequest();
        final KeycloakSecurityContext securityContext =
                (KeycloakSecurityContext)
                        httpRequest.getAttribute(KeycloakSecurityContext.class.getName());
        return securityContext.getTokenString();
    }

    @Bean
    public UserProvider getUserProvider() {
        return (Region region) -> {
            final AccessToken token = getAccessToken();
            final String tokenString = getAccessTokenString();
            final User user =
                    User.newInstance()
                            .addGroups(getGroupsFromToken(region, token))
                            .setEmail(token.getEmail())
                            .setNomComplet(token.getName())
                            .setIdep(token.getPreferredUsername())
                            .setIp(
                                    httpRequestUtils.getClientIpAddressIfServletRequestExist(
                                            ((ServletRequestAttributes)
                                                            RequestContextHolder
                                                                    .currentRequestAttributes())
                                                    .getRequest()))
                            .build();
            user.getAttributes().putAll(token.getOtherClaims());
            user.getAttributes().put("sub", token.getSubject());
            user.getAttributes().put("preferred_username", token.getPreferredUsername());
            user.getAttributes().put("access_token", tokenString);
            return user;
        };
    }

    private List<String> getGroupsFromToken(Region region, final AccessToken token) {
        List<String> groups =
                ((List<?>) token.getOtherClaims().getOrDefault("groups", List.of()))
                        .stream().map(String.class::cast).collect(Collectors.toList());
        if (region.getExcludedGroupPattern() != null) {
            Pattern excludePattern = Pattern.compile(region.getExcludedGroupPattern());
            groups.removeIf(group -> excludePattern.matcher(group).matches());
        }
        if (region.getIncludedGroupPattern() != null) {
            Pattern includePattern = Pattern.compile(region.getIncludedGroupPattern());
            groups.removeIf(group -> !includePattern.matcher(group).matches());
        }
        if (region.getIncludedGroupPattern() != null && region.getTransformGroupPattern() != null) {
            Pattern includePattern = Pattern.compile(region.getIncludedGroupPattern());
            groups =
                    groups.stream()
                            .map(
                                    group ->
                                            transformGroupFromProviderGroup(
                                                    includePattern,
                                                    region.getTransformGroupPattern(),
                                                    group))
                            .collect(Collectors.toList());
        }
        return groups.stream()
                .filter(this::isRespectingRFC1123LabelName)
                .collect(Collectors.toList());
    }

    private String transformGroupFromProviderGroup(
            Pattern includePattern, String extractPattern, String providerGroup) {
        try {
            return includePattern.matcher(providerGroup).replaceAll(extractPattern);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isRespectingRFC1123LabelName(String string) {
        return string != null && string.length() <= 63 && rfc1123Pattern.matcher(string).matches();
    }
}
