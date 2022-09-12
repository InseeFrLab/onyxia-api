package fr.insee.onyxia.api.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.Catalogs;
import fr.insee.onyxia.api.services.CatalogService;
import fr.insee.onyxia.model.catalog.Pkg;

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

}
