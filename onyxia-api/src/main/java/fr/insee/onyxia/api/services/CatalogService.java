package fr.insee.onyxia.api.services;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.Catalogs;
import fr.insee.onyxia.model.catalog.Pkg;

public interface CatalogService {

   public Catalogs getCatalogs();

   public CatalogWrapper getCatalogById(String catalogId);

   public Pkg getPackage(String catalogId, String packageName);
}
