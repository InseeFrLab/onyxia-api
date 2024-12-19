package fr.insee.onyxia.api.security;

import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.api.services.UserProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class LogUserInfoFilter extends OncePerRequestFilter {

    private UserProvider userProvider;

    private RegionsConfiguration regionsConfiguration;

    @Autowired
    public LogUserInfoFilter(UserProvider userProvider, RegionsConfiguration regionsConfiguration) {
        this.userProvider = userProvider;
        this.regionsConfiguration = regionsConfiguration;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            MDC.put(
                    "username",
                    userProvider.getUser(regionsConfiguration.getDefaultRegion()).getIdep());
        } catch (Exception ignored) {

        }
        filterChain.doFilter(request, response);
        MDC.clear();
    }
}
