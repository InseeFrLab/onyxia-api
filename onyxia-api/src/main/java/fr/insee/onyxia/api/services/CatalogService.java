package fr.insee.onyxia.api.services;

import java.util.List;
import java.util.Optional;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.Catalogs;
import fr.insee.onyxia.model.catalog.Pkg;
import fr.insee.onyxia.model.helm.Chart;

public interface CatalogService {

   public Catalogs getCatalogs();

   public CatalogWrapper getCatalogById(String catalogId);

   public Pkg getPackage(String catalogId, String packageName);

   public Optional<Chart> getChartByVersion(String catalogId, String chartName, String version);

   public Optional<List<Chart>> getCharts(String catalogId, String chartName);
}
