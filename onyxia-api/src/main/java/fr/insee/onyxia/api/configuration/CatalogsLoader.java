package fr.insee.onyxia.api.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.onyxia.api.configuration.properties.CatalogsConfiguration;
import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.model.region.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class CatalogsLoader {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CatalogFilter catalogFilter;

    @Autowired
    private CatalogsConfiguration catalogsConfiguration;

    private static Logger logger = LoggerFactory.getLogger(CatalogsLoader.class);

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public Catalogs catalogs() {
        Catalogs catalogs = new Catalogs();
        catalogs.setCatalogs(catalogsConfiguration.getResolvedCatalogs());
        catalogs.setCatalogs(catalogFilter.filterCatalogs(catalogs.getCatalogs()));
        logger.info("Serving "+catalogs.getCatalogs().size()+" catalogs");
        return catalogs;
    }

    @Service
    public class CatalogFilter {

        @Autowired
        private RegionsConfiguration regionsConfiguration;

        private Logger logger = LoggerFactory.getLogger(CatalogFilter.class);

        public List<CatalogWrapper> filterCatalogs(List<CatalogWrapper> catalogs) {
            List<Region> regions = regionsConfiguration.getResolvedRegions();
            boolean marathonEnabled = regions.stream().filter(region -> region.getServices().getType().equals(fr.insee.onyxia.model.service.Service.ServiceType.MARATHON)).count() > 0;
            boolean kubernetesEnabled = regions.stream().filter(region -> region.getServices().getType().equals(fr.insee.onyxia.model.service.Service.ServiceType.KUBERNETES)).count() > 0;

            logger.info("Marathon support enabled : " + marathonEnabled);
            logger.info("Kubernetes support enabled : " + kubernetesEnabled);
            return catalogs.stream().filter(cw -> {
                if (cw.getType().equals("universe") && marathonEnabled) {
                    return true;
                }
                if (cw.getType().equals("helm") && kubernetesEnabled) {
                    return true;
                }
                logger.info("Filtering out catalog " + cw.getName());
                return false;
            }).collect(Collectors.toList());
        }
    }

}
