package fr.insee.onyxia.api.controller.pub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.Catalogs;
import fr.insee.onyxia.api.configuration.NotFoundException;
import fr.insee.onyxia.api.services.CatalogService;
import fr.insee.onyxia.model.catalog.Package;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Catalog", description = "Catalog")
@RequestMapping("/public/catalog")
@RestController
public class CatalogController {

   @Autowired
   private CatalogService catalogService;

   @GetMapping
   public Catalogs getCatalogs() {
      return catalogService.getCatalogs();
   }

   @GetMapping("{catalogId}")
   public CatalogWrapper getCatalogById(@PathVariable String catalogId) {
      CatalogWrapper wrapper = catalogService.getCatalogById(catalogId);
      if (wrapper == null) {
         throw new NotFoundException();
      }
      return wrapper;
   }

   @GetMapping("{catalogId}/{packageName}")
   public Package getPackage(@PathVariable String catalogId, @PathVariable String packageName) {
      Package pkg = catalogService.getPackage(catalogId, packageName);
      if (pkg == null) {
         throw new NotFoundException();
      }
      return pkg;
   }

}
