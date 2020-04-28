package fr.insee.onyxia.api.configuration.properties;

import fr.insee.onyxia.model.service.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudshellConfiguration {

    @Autowired
    private OrchestratorConfiguration orchestratorConfiguration;

    @Value("${cloudshell.catalogid}")
    private String catalogId;

    @Value("${cloudshell.packagename}")
    private String packageName;

    public String getCatalogId() {
        if (!StringUtils.isBlank(catalogId)) {
            return catalogId;
        }
        if (orchestratorConfiguration.getPreferredServiceType() == Service.ServiceType.MARATHON) {
            return "internal";
        }
        else {
            return "inseefrlab-helm-charts";
        }
    }

    public String getPackageName() {
        if (!StringUtils.isBlank(packageName)) {
            return packageName;
        }
        if (orchestratorConfiguration.getPreferredServiceType() == Service.ServiceType.MARATHON) {
            return "shelly";
        }
        else {
            return "cloudshell";
        }
    }
}
