package fr.insee.onyxia.api.services;

import fr.insee.onyxia.api.configuration.Multiverse;
import fr.insee.onyxia.api.configuration.UniverseWrapper;
import fr.insee.onyxia.model.catalog.UniversePackage;

public interface CatalogService {

   public Multiverse getCatalogs();
   
   public UniverseWrapper getCatalogById(String catalogId);
   
   public UniversePackage getPackage(String catalogId,String packageName);
}
