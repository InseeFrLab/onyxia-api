package fr.insee.onyxia.api.configuration;

import fr.insee.onyxia.api.controller.api.utils.ProjectResolver;
import fr.insee.onyxia.api.controller.api.utils.RegionResolver;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private final RegionResolver regionResolver;

    private final ProjectResolver projectResolver;

    @Autowired
    public WebConfiguration(RegionResolver regionResolver, ProjectResolver projectResolver) {
        this.regionResolver = regionResolver;
        this.projectResolver = projectResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(regionResolver);
        argumentResolvers.add(projectResolver);
    }
}
