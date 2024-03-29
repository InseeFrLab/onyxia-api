package fr.insee.onyxia.api.controller.api.utils;

import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.model.region.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Service
public class RegionResolver implements HandlerMethodArgumentResolver {

    private final RegionsConfiguration regionsConfiguration;

    @Autowired
    public RegionResolver(RegionsConfiguration regionsConfiguration) {
        this.regionsConfiguration = regionsConfiguration;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterType() == Region.class;
    }

    @Override
    public Object resolveArgument(
            MethodParameter methodParameter,
            ModelAndViewContainer modelAndViewContainer,
            NativeWebRequest nativeWebRequest,
            WebDataBinderFactory webDataBinderFactory) {
        String region = nativeWebRequest.getHeader("ONYXIA-REGION");
        Region defaultRegion = regionsConfiguration.getDefaultRegion();
        if (region != null) {
            return regionsConfiguration.getRegionById(region).orElse(defaultRegion);
        }
        return defaultRegion;
    }
}
