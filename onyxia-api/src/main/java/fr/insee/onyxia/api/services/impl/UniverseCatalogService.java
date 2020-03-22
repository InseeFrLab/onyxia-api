package fr.insee.onyxia.api.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.onyxia.api.services.CatalogService;
import fr.insee.onyxia.model.catalog.Multiverse;
import fr.insee.onyxia.model.catalog.UniversePackage;
import fr.insee.onyxia.model.catalog.UniverseWrapper;

@Service
public class UniverseCatalogService implements CatalogService {

   @Autowired
   private Multiverse multiverse;

   @Override
   public Multiverse getCatalogs() {
      return multiverse;
   }

   @Override
   public UniverseWrapper getCatalogById(String catalogId) {
      return multiverse.getUniverseById(catalogId);
   }

   @Override
   public UniversePackage getPackage(String catalogId, String packageName) {
      return multiverse.getUniverseById(catalogId).getUniverse().getPackageByName(packageName);
   }

}
