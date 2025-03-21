package fr.insee.onyxia.api.controller.api.mylab;

import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.impl.kubernetes.KubernetesService;
import fr.insee.onyxia.api.services.impl.kubernetes.KubernetesService.Owner;
import fr.insee.onyxia.model.project.Project;
import fr.insee.onyxia.model.region.Region;
import fr.insee.onyxia.model.service.quota.Quota;
import fr.insee.onyxia.model.service.quota.QuotaUsage;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceQuota;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "My lab", description = "My services")
@RequestMapping("/my-lab/quota")
@RestController
@SecurityRequirement(name = "auth")
public class QuotaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuotaController.class);

    private final KubernetesService kubernetesService;

    private final UserProvider userProvider;

    @Autowired
    public QuotaController(KubernetesService kubernetesService, UserProvider userProvider) {
        this.kubernetesService = kubernetesService;
        this.userProvider = userProvider;
    }

    @Operation(
            summary = "Obtain the quota for a namespace.",
            description =
                    "Obtain both the quota limit for a namespace and the current quota usage, if quota limits are enabled on Onyxia.",
            parameters = {
                @Parameter(
                        name = "ONYXIA-PROJECT",
                        description =
                                "Project associated with the namespace, defaults to user project.",
                        in = ParameterIn.HEADER,
                        schema =
                                @Schema(
                                        name = "ONYXIA-PROJECT",
                                        type = "string",
                                        description = "generated project id"))
            })
    @GetMapping
    public QuotaUsage getQuota(
            @Parameter(hidden = true) Region region, @Parameter(hidden = true) Project project) {
        final Owner owner = getOwner(region, project);
        ResourceQuota resourceQuota = null;
        if (owner.getType() == Owner.OwnerType.USER) {
            checkUserQuotaIsEnabled(region);
            resourceQuota =
                    kubernetesService.getOnyxiaQuota(region, project, userProvider.getUser(region));
        } else if (owner.getType() == Owner.OwnerType.GROUP) {
            checkGroupQuotaIsEnabled(region);
            resourceQuota =
                    kubernetesService.getOnyxiaQuota(region, project, userProvider.getUser(region));
        }
        if (resourceQuota == null) {
            return new QuotaUsage();
        }

        final QuotaUsage quotaUsage = new QuotaUsage();
        final Quota spec = kubQuotaToQuota(resourceQuota.getStatus().getHard());
        final Quota usage = kubQuotaToQuota(resourceQuota.getStatus().getUsed());
        quotaUsage.setSpec(spec);
        quotaUsage.setUsage(usage);

        return quotaUsage;
    }

    private void checkUserQuotaIsEnabled(Region region) {
        if (!region.getServices().getQuotas().isUserEnabled()) {
            throw new AccessDeniedException("User Quotas are not active on this installation");
        }
    }

    private void checkGroupQuotaIsEnabled(Region region) {
        if (!region.getServices().getQuotas().isGroupEnabled()) {
            throw new AccessDeniedException("Group Quotas are not active on this installation");
        }
    }

    private Owner getOwner(Region region, Project project) {
        final KubernetesService.Owner owner = new KubernetesService.Owner();
        if (project.getGroup() != null) {
            owner.setId(project.getGroup());
            owner.setType(Owner.OwnerType.GROUP);
        } else {
            owner.setId(userProvider.getUser(region).getIdep());
            owner.setType(KubernetesService.Owner.OwnerType.USER);
        }
        return owner;
    }

    private Quota kubQuotaToQuota(Map<String, Quantity> resourceQuota) {
        final Map<String, String> rawData = new HashMap<>();
        resourceQuota.forEach((key, value) -> rawData.put(key, value.toString()));
        return Quota.from(rawData);
    }

    public KubernetesService getKubernetesService() {
        return kubernetesService;
    }

    public UserProvider getUserProvider() {
        return userProvider;
    }
}
