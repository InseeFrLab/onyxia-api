package fr.insee.onyxia.api.security;

import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.api.services.UserProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class LogUserInfoFilter extends OncePerRequestFilter {

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private RegionsConfiguration regionsConfiguration;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        MDC.put("username", userProvider.getUser(regionsConfiguration.getDefaultRegion()).getIdep());
        filterChain.doFilter(request, response);
        MDC.clear();
    }
}