package fr.insee.onyxia.api.services;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.Catalogs;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.helm.Chart;
import fr.insee.onyxia.model.region.Region;
import java.util.List;
import java.util.Optional;

public interface CatalogService {

    default Catalogs getCatalogs() {
        return getCatalogs(null, null);
    }

    default Catalogs getCatalogs(Region region) {
        return getCatalogs(region, null);
    }

    Catalogs getCatalogs(Region region, User user);

    CatalogWrapper getCatalogById(String catalogId);

    Optional<Chart> getPackage(String catalogId, String packageName);

    Optional<Chart> getChartByVersion(String catalogId, String chartName, String version);

    Optional<List<Chart>> getCharts(String catalogId, String chartName);
}
