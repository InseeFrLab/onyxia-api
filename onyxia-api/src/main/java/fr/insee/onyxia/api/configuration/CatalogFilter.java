package fr.insee.onyxia.api.configuration;

import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CatalogFilter {

    private final RegionsConfiguration regionsConfiguration;

    private Logger LOGGER = LoggerFactory.getLogger(CatalogFilter.class);

    public CatalogFilter(RegionsConfiguration regionsConfiguration) {
        this.regionsConfiguration = regionsConfiguration;
    }

    public List<CatalogWrapper> filterCatalogs(List<CatalogWrapper> catalogs) {
        return catalogs;
    }
}
