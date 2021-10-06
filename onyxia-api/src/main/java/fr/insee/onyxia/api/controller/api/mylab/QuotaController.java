package fr.insee.onyxia.api.controller.api.mylab;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.impl.kubernetes.KubernetesService;
import fr.insee.onyxia.model.project.Project;
import fr.insee.onyxia.model.region.Region;
import fr.insee.onyxia.model.service.quota.Quota;
import fr.insee.onyxia.model.service.quota.QuotaUsage;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceQuota;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "My lab", description = "My services")
@RequestMapping("/my-lab/quota")
@RestController
@SecurityRequirement(name = "auth")
public class QuotaController {

    @Autowired
    private KubernetesService kubernetesService;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    public QuotaUsage getQuota(@Parameter(hidden = true) Region region, @Parameter(hidden=true) Project project) {
        ResourceQuota resourceQuota = kubernetesService.getOnyxiaQuota(region, project, userProvider.getUser());
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
