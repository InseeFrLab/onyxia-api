package fr.insee.onyxia.api.configuration;

import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogFilter {

    @Autowired
    private RegionsConfiguration regionsConfiguration;

    private Logger logger = LoggerFactory.getLogger(CatalogFilter.class);

    public List<CatalogWrapper> filterCatalogs(List<CatalogWrapper> catalogs) {
        return catalogs;
    }
}
