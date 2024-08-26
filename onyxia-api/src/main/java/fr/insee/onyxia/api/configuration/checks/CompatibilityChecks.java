package fr.insee.onyxia.api.configuration.checks;

import fr.insee.onyxia.api.configuration.kubernetes.KubernetesClientProvider;
import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(CompatibilityChecks.class);
    private final RegionsConfiguration regionsConfiguration;
    private final KubernetesClientProvider kubernetesClientProvider;
    private final HelmVersionService helmVersionService;
    private final Runnable exitHandler;

    @Autowired
    public CompatibilityChecks(
            RegionsConfiguration regionsConfiguration,
            KubernetesClientProvider kubernetesClientProvider,
            HelmVersionService helmVersionService) {
        this(
                regionsConfiguration,
                kubernetesClientProvider,
                helmVersionService,
                () -> System.exit(0));
    }

    public CompatibilityChecks(
            RegionsConfiguration regionsConfiguration,
            KubernetesClientProvider kubernetesClientProvider,
            HelmVersionService helmVersionService,
            Runnable exitHandler) {
        this.regionsConfiguration = regionsConfiguration;
        this.kubernetesClientProvider = kubernetesClientProvider;
        this.helmVersionService = helmVersionService;
        this.exitHandler = exitHandler;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void checkHelm() {
        try {
            LOGGER.info("Using helm {}", helmVersionService.getVersion());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Thread was interrupted while determining helm version", e);
        } catch (Exception e) {
            LOGGER.error("Could not determine helm version", e);
            exitHandler.run();
        }
    }

    @EventListener(ContextRefreshedEvent.class)
    public void checkKubernetesVersion() {
        regionsConfiguration
                .getResolvedRegions()
                .forEach(
                        region -> {
                            KubernetesClient client =
                                    kubernetesClientProvider.getRootClient(region);
                            try {
                                LOGGER.info(
                                        "Region {} kubernetes v{}.{}",
                                        region.getName(),
                                        client.getKubernetesVersion().getMajor(),
                                        client.getKubernetesVersion().getMinor());
                            } catch (Exception e) {
                                LOGGER.error(
                                        "Could not contact Kubernetes APIServer for region {} at {}",
                                        region.getName(),
                                        client.getMasterUrl(),
                                        e);
                            }
                        });
    }
}
