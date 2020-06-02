package fr.insee.onyxia.api.controller.pub;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.Catalogs;
import fr.insee.onyxia.api.configuration.NotFoundException;
import fr.insee.onyxia.api.services.CatalogService;
import fr.insee.onyxia.model.catalog.Config.Property;
import fr.insee.onyxia.model.catalog.Package;
import fr.insee.onyxia.model.region.Region;
import fr.insee.onyxia.model.service.Service;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Public")
@RequestMapping("/public/catalog")
@RestController
public class CatalogController {

   @Autowired
   private CatalogService catalogService;

   @GetMapping
   public Catalogs getCatalogs(@Parameter(hidden = true) Region region) {
      Catalogs filteredCatalogs = new Catalogs();
      filteredCatalogs.setCatalogs(catalogService.getCatalogs().getCatalogs().stream().filter((catalog) -> isCatalogEnabled(region, catalog)).collect(Collectors.toList()));
      return filteredCatalogs;
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
      addCustomOnyxiaProperties(pkg);
      if (pkg == null) {
         throw new NotFoundException();
      }
      return pkg;
   }

   private boolean isCatalogEnabled(Region region, CatalogWrapper catalog) {
      if (region == null) {
         return true;
      }

      if (catalog.getType().equalsIgnoreCase("UNIVERSE")) {
         return region.getServices().getType().equals(Service.ServiceType.MARATHON);
      }

      if (catalog.getType().equalsIgnoreCase("HELM")) {
         return region.getServices().getType().equals(Service.ServiceType.KUBERNETES);
      }

      return false;
   }

   private void addCustomOnyxiaProperties(Package pkg) {
      Map<String, Property> properties = pkg.getConfig().getProperties().getProperties();
      Property property = new Property();
      property.setDescription("Onyxia specific configuration");
      property.setType("object");
      property.setProperties(new HashMap<>());
      Map<String,Property> onyxiaProperties = new HashMap<>();
      Property customName = new Property();
      customName.setType("string");
      customName.setDescription("Service custom name");
      customName.setDefaut(pkg.getName());
      customName.setTitle("Custom name");
      onyxiaProperties.put("friendlyName", customName);
      property.setProperties(onyxiaProperties);

      properties.put("onyxia",property);
   }

}
