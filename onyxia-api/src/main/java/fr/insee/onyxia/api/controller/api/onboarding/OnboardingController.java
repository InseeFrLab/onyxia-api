package fr.insee.onyxia.api.controller.api.onboarding;

import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.impl.kubernetes.KubernetesService;
import fr.insee.onyxia.model.region.Region;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Onboarding", description = "Onboarding related services")
@RequestMapping(value={"/api/onboarding", "/onboarding"})
@RestController
@SecurityRequirement(name = "auth")
public class OnboardingController {

    @Autowired
    private KubernetesService kubernetesService;

    @Autowired
    private UserProvider userProvider;

    @PostMapping
    public void onboard(@Parameter(hidden = true) Region region, @RequestBody OnboardingRequest request) {
        checkPermissions(region, request);
        KubernetesService.Owner owner = new KubernetesService.Owner();
        if (request.getGroup() != null) {
            owner.setId(request.getGroup());
            owner.setType(KubernetesService.Owner.OwnerType.GROUP);
        }
        else {
            owner.setId(userProvider.getUser(region).getIdep());
            owner.setType(KubernetesService.Owner.OwnerType.USER);
        }
        kubernetesService.createDefaultNamespace(region, owner);
    }

    private void checkPermissions(Region region, OnboardingRequest request) throws AccessDeniedException {
        if (request.getGroup() != null
                && !userProvider.getUser(region).getGroups().contains(request.getGroup())) {
            throw new AccessDeniedException("User does not belong to group " + request.getGroup());
        }
    }

    public static class OnboardingRequest {

        private String group;

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }
    }
}
