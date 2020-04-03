package fr.insee.onyxia.api.services;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.Catalogs;
import fr.insee.onyxia.model.catalog.Package;

public interface CatalogService {

   public Catalogs getCatalogs();

   public CatalogWrapper getCatalogById(String catalogId);

   public Package getPackage(String catalogId, String packageName);
}
