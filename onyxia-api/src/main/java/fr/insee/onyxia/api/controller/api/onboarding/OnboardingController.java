package fr.insee.onyxia.api.controller.api.onboarding;

import fr.insee.onyxia.api.controller.exception.NamespaceAlreadyExistException;
import fr.insee.onyxia.api.controller.exception.OnboardingDisabledException;
import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.impl.kubernetes.KubernetesService;
import fr.insee.onyxia.model.region.Region;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Onboarding", description = "Onboarding related services")
@RequestMapping("/onboarding")
@RestController
@SecurityRequirement(name = "auth")
public class OnboardingController {

    private final KubernetesService kubernetesService;

    private final UserProvider userProvider;

    @Autowired
    public OnboardingController(KubernetesService kubernetesService, UserProvider userProvider) {
        this.kubernetesService = kubernetesService;
        this.userProvider = userProvider;
    }

    @Operation(
            summary = "Init a namespace for a user or a group.",
            description =
                    "create or replace the namespace of the user or the namespace of a group if the user is in the requested group and the according rbac policies. with the group prefix / user prefix of the region",
            parameters = {
                @Parameter(
                        required = false,
                        name = "ONYXIA-REGION",
                        description =
                                "The region used by the user, if not provided default to the first region configured.",
                        in = ParameterIn.HEADER,
                        schema = @Schema(name = "ONYXIA-REGION", type = "string"))
            })
    @PostMapping
    public void onboard(
            @Parameter(hidden = true) Region region, @RequestBody OnboardingRequest request) {
        if (region.getServices().isSingleNamespace()) {
            throw new NamespaceAlreadyExistException();
        }
        if (!region.getServices().isAllowNamespaceCreation()) {
            throw new OnboardingDisabledException();
        }

        checkPermissions(region, request);
        final KubernetesService.Owner owner = new KubernetesService.Owner();
        if (request.getGroup() != null) {
            owner.setId(request.getGroup());
            owner.setType(KubernetesService.Owner.OwnerType.GROUP);
        } else {
            owner.setId(userProvider.getUser(region).getIdep());
            owner.setType(KubernetesService.Owner.OwnerType.USER);
        }
        kubernetesService.createDefaultNamespace(region, owner);
    }

    private void checkPermissions(Region region, OnboardingRequest request)
            throws AccessDeniedException {
        if (request.getGroup() != null
                && !userProvider.getUser(region).getGroups().contains(request.getGroup())) {
            throw new AccessDeniedException("User does not belong to group " + request.getGroup());
        }
    }

    @Schema(
            description =
                    "Specification on which namespace to create. If group is provided, create a group namespace, otherwise create the user namespace.")
    public static class OnboardingRequest {

        @Schema(required = false)
        private String group;

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }
    }
}
