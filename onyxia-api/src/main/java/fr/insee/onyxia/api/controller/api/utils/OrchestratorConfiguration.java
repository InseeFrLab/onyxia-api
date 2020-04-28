package fr.insee.onyxia.api.controller.api.utils;

import fr.insee.onyxia.model.service.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrchestratorConfiguration {


    @Value("${kubernetes.enabled}")
    private boolean KUB_ENABLED;

    @Value("${marathon.enabled}")
    private boolean MARATHON_ENABLED;

    public boolean isMarathonEnabled() {
        return MARATHON_ENABLED;
    }

    public boolean isKubernetesEnabled() {
        return KUB_ENABLED;
    }

    public Service.ServiceType getPreferredServiceType() {
        if (MARATHON_ENABLED && !KUB_ENABLED) {
            return Service.ServiceType.MARATHON;
        }

        if (!MARATHON_ENABLED && KUB_ENABLED) {
            return Service.ServiceType.KUBERNETES;
        }

        return Service.ServiceType.MARATHON;
    }

}
