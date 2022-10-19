package fr.insee.onyxia.api.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.Catalogs;
import fr.insee.onyxia.api.services.CatalogService;
import fr.insee.onyxia.model.catalog.Pkg;
import fr.insee.onyxia.model.helm.Chart;

@Service
public class CatalogServiceImpl implements CatalogService {

   @Autowired
   private Catalogs catalogs;

   @Override
   public Catalogs getCatalogs() {
      return catalogs;
   }

   @Override
   public CatalogWrapper getCatalogById(String catalogId) {
      return catalogs.getCatalogById(catalogId);
   }

   @Override
   public Pkg getPackage(String catalogId, String packageName) {
      return catalogs.getCatalogById(catalogId).getCatalog().getPackageByName(packageName);
   }

   @Override
   public Optional<List<Chart>> getCharts(String catalogId, String chartName) {
      return Optional.ofNullable(catalogs.getCatalogById(catalogId).getCatalog().getEntries().get(chartName));
   }

   @Override
   public Optional<Chart> getChartByVersion(String catalogId, String chartName, String version) {
      List<Chart> charts = catalogs.getCatalogById(catalogId).getCatalog().getEntries().get(chartName);
      if (charts != null) {
         return charts.stream().filter(c -> c.getVersion().equalsIgnoreCase(version)).findAny();
      }
      else return Optional.empty();
   }

}
