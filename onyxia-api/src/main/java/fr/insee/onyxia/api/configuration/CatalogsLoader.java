package fr.insee.onyxia.api.configuration;

import fr.insee.onyxia.api.configuration.properties.CatalogsConfiguration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class CatalogsLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogsLoader.class);

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public Catalogs catalogs(CatalogsConfiguration catalogsConfiguration) {
        List<CatalogWrapper> resolvedCatalogs = catalogsConfiguration.getResolvedCatalogs();
        LOGGER.info("Serving {} catalogs", resolvedCatalogs.size());
        return new Catalogs(resolvedCatalogs);
    }
}
