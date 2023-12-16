package fr.insee.onyxia.api.services;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.Catalogs;
import fr.insee.onyxia.model.catalog.Pkg;
import fr.insee.onyxia.model.helm.Chart;
import java.util.List;
import java.util.Optional;

public interface CatalogService {

    Catalogs getCatalogs();

    CatalogWrapper getCatalogById(String catalogId);

    Optional<Pkg> getPackage(String catalogId, String packageName);

    Optional<Chart> getChartByVersion(String catalogId, String chartName, String version);

    Optional<List<Chart>> getCharts(String catalogId, String chartName);
}
