package fr.insee.onyxia.api.controller.api.utils;

import fr.insee.onyxia.api.user.OnyxiaUserProvider;
import fr.insee.onyxia.model.OnyxiaUser;
import fr.insee.onyxia.model.project.Project;
import fr.insee.onyxia.model.region.Region;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Service
public class ProjectResolver implements HandlerMethodArgumentResolver {

    private final RegionResolver regionResolver;

    private final OnyxiaUserProvider userProvider;

    @Autowired
    public ProjectResolver(RegionResolver regionResolver, OnyxiaUserProvider userProvider) {
        this.regionResolver = regionResolver;
        this.userProvider = userProvider;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterType() == Project.class;
    }

    @Override
    public Object resolveArgument(
            MethodParameter methodParameter,
            ModelAndViewContainer modelAndViewContainer,
            NativeWebRequest nativeWebRequest,
            WebDataBinderFactory webDataBinderFactory)
            throws Exception {
        String project = nativeWebRequest.getHeader("ONYXIA-PROJECT");
        OnyxiaUser user =
                userProvider.getUser(
                        (Region)
                                regionResolver.resolveArgument(
                                        methodParameter,
                                        modelAndViewContainer,
                                        nativeWebRequest,
                                        webDataBinderFactory));

        if (StringUtils.isBlank(project)) {
            return user.getProjects().getFirst();
        } else {
            Project resolvedProject =
                    user.getProjects().stream()
                            .filter(pr -> pr.getId().equalsIgnoreCase(project))
                            .findFirst()
                            .orElse(null);
            if (resolvedProject == null) {
                throw new AccessDeniedException(
                        "User does not have permission on project " + project);
            }
            return resolvedProject;
        }
    }
}
