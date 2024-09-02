package fr.insee.onyxia.api.configuration.checks;

import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
public class V3Checks {

    private static final Logger LOGGER = LoggerFactory.getLogger(V3Checks.class);

    private final RegionsConfiguration regionsConfiguration;

    private final Runnable exitHandler;

    @Autowired
    public V3Checks(RegionsConfiguration regionsConfiguration) {
        this(regionsConfiguration,  () -> System.exit(0));
    }

    public V3Checks(
            RegionsConfiguration regionsConfiguration, Runnable exitHandler) {
        this.regionsConfiguration = regionsConfiguration;
        this.exitHandler = exitHandler;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void checkDefaultConfigurationIsNoLongerSupported() {
        regionsConfiguration.getResolvedRegions().forEach(region -> {
           if (region.getServices().getDefaultConfiguration() != null) {
               LOGGER.error("FATAL : Setting defaultConfiguration in region is no longer supported and has been replaced by JSONSchema support. See migration guide at https://docs.onyxia.sh/admin-doc/migration-guides/v8-greater-than-v9");
               exitHandler.run();
           }
        });
    }
}
