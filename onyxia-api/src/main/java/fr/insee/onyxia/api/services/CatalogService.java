package fr.insee.onyxia.api.services;

import fr.insee.onyxia.model.catalog.Multiverse;
import fr.insee.onyxia.model.catalog.UniversePackage;
import fr.insee.onyxia.model.catalog.UniverseWrapper;

public interface CatalogService {

   public Multiverse getCatalogs();
   
   public UniverseWrapper getCatalogById(String catalogId);
   
   public UniversePackage getPackage(String catalogId,String packageName);
}
