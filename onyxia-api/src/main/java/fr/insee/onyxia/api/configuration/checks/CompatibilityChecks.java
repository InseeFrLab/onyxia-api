package fr.insee.onyxia.api.configuration.checks;

import fr.insee.onyxia.api.configuration.kubernetes.KubernetesClientProvider;
import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.model.service.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.github.inseefrlab.helmwrapper.service.HelmVersionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
public class CompatibilityChecks {

    private static Logger LOGGER = LoggerFactory.getLogger(RegionsConfiguration.class);
    @Autowired RegionsConfiguration regionsConfiguration;
    @Autowired KubernetesClientProvider kubernetesClientProvider;

    @Autowired HelmVersionService helmVersionService;

    @EventListener(ContextRefreshedEvent.class)
    public void checkHelm() {
        try {
            LOGGER.info("Using helm " + helmVersionService.getVersion());
        } catch (Exception e) {
            LOGGER.error("Could not determine helm version", e);
            System.exit(0);
        }
    }

    @EventListener(ContextRefreshedEvent.class)
    public void checkKubernetesVersion() {
        regionsConfiguration
                .getResolvedRegions()
                .forEach(
                        region -> {
                            if (region.getServices()
                                    .getType()
                                    .equals(Service.ServiceType.KUBERNETES)) {
                                KubernetesClient client =
                                        kubernetesClientProvider.getRootClient(region);
                                try {
                                    LOGGER.info(
                                            "Region "
                                                    + region.getName()
                                                    + " kubernetes v"
                                                    + client.getKubernetesVersion().getMajor()
                                                    + "."
                                                    + client.getKubernetesVersion().getMinor());
                                } catch (Exception e) {
                                    LOGGER.error(
                                            "Could not contact Kubernetes APIServer for region "
                                                    + region.getName()
                                                    + " at "
                                                    + client.getMasterUrl(),
                                            e);
                                }
                            }
                        });
    }
}
