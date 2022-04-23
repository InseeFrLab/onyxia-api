package fr.insee.onyxia.api.controller.api.mylab;

import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.impl.kubernetes.KubernetesService;
import fr.insee.onyxia.model.project.Project;
import fr.insee.onyxia.model.region.Region;
import fr.insee.onyxia.model.service.quota.Quota;
import fr.insee.onyxia.model.service.quota.QuotaUsage;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceQuota;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "My lab", description = "My services")
@RequestMapping(value={"/api/my-lab/quota", "/my-lab/quota"})
@RestController
@SecurityRequirement(name = "auth")
public class QuotaController {

    @Autowired
    private KubernetesService kubernetesService;

    @Autowired
    private UserProvider userProvider;

    @Operation(
        summary = "Obtain the quota for a namespace.",
        description = "Obtain both the quota limit for a namespace and the current quota usage, if quota limits are enabled on Onyxia.",
        parameters = {
            @Parameter(
                required = false,
                name = "ONYXIA-PROJECT",
                description = "Project associated with the namespace, defaults to user project.",
                in = ParameterIn.HEADER,
                schema = @Schema(
                    name = "ONYXIA-PROJECT",
                    type = "string",
                    description = "generated project id"
                )
            )
        }
    )
    @GetMapping
    public QuotaUsage getQuota(@Parameter(hidden = true) Region region, @Parameter(hidden = true) Project project) {
        checkQuotaIsEnabled(region);
        ResourceQuota resourceQuota = kubernetesService.getOnyxiaQuota(region, project, userProvider.getUser(region));
        if (resourceQuota == null) {
            return null;
        }

        QuotaUsage quotaUsage = new QuotaUsage();
        Quota spec  = new Quota();
        Quota usage = new Quota();
        mapKubQuotaToQuota(resourceQuota.getStatus().getHard(), spec);
        mapKubQuotaToQuota(resourceQuota.getStatus().getUsed(), usage);
        quotaUsage.setSpec(spec);
        quotaUsage.setUsage(usage);

        return quotaUsage;
    }

    @Operation(
        summary = "Change the quota for a namespace.",
        description = "Change the quota for a namespace if the quota changing option is enabled.",
        parameters = {
            @Parameter(
                required = false,
                name = "ONYXIA-PROJECT",
                description = "Project associated with the namespace, defaults to user project.",
                in = ParameterIn.HEADER,
                schema = @Schema(
                    name = "ONYXIA-PROJECT",
                    type = "string",
                    description = "generated project id"
                )
            )
        }
    )
    @PostMapping
    public void applyQuota(@Parameter(hidden = true) Region region, @Parameter(hidden=true) Project project, @RequestBody Quota quota) throws IllegalAccessException {
        checkQuotaIsEnabled(region);
        checkQuotaModificationIsAllowed(region);
        kubernetesService.applyQuota(region, project, userProvider.getUser(region), quota);
    }

    @Operation(
        summary = "Reset the quota for a namespace to the default value.",
        description = "Reset the quota for a namespace to the default value if the quota changing option is enabled.",
        parameters = {
            @Parameter(
                required = false,
                name = "ONYXIA-PROJECT",
                description = "Project associated with the namespace, defaults to user project.",
                in = ParameterIn.HEADER,
                schema = @Schema(
                    name = "ONYXIA-PROJECT",
                    type = "string",
                    description = "generated project id"
                )
            )
        }
    )
    @PostMapping("/reset")
    public void resetQuota(@Parameter(hidden = true) Region region, @Parameter(hidden=true) Project project) {
        checkQuotaIsEnabled(region);
        checkQuotaModificationIsAllowed(region);
        if (!region.getServices().getQuotas().isAllowUserModification()) {
            throw new AccessDeniedException("User modification is not allowed on this installation");
        }
        kubernetesService.applyQuota(region, project, userProvider.getUser(region),
                region.getServices().getQuotas().getDefaultQuota());
    }

    private void checkQuotaIsEnabled(Region region) {
        if (!region.getServices().getQuotas().isEnabled()) {
            throw new AccessDeniedException("Quotas are not active on this installation");
        }
    }

    private void checkQuotaModificationIsAllowed(Region region) {
        if (!region.getServices().getQuotas().isAllowUserModification()) {
            throw new AccessDeniedException("User modification is not allowed on this installation");
        }
    }

    private void mapKubQuotaToQuota(Map<String, Quantity> resourceQuota, Quota quota) {
        Map<String, String> rawData = new HashMap<>();
        resourceQuota.entrySet().forEach(e -> rawData.put(e.getKey(), e.getValue().toString()));
        quota.loadFromMap(rawData);
    }

    public KubernetesService getKubernetesService() {
        return kubernetesService;
    }

    public void setKubernetesService(KubernetesService kubernetesService) {
        this.kubernetesService = kubernetesService;
    }

    public UserProvider getUserProvider() {
        return userProvider;
    }

    public void setUserProvider(UserProvider userProvider) {
        this.userProvider = userProvider;
    }
}
