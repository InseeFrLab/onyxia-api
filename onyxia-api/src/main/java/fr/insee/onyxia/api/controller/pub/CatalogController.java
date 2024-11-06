package fr.insee.onyxia.api.controller.pub;

import fr.insee.onyxia.api.configuration.Catalogs;
import fr.insee.onyxia.api.services.CatalogService;
import fr.insee.onyxia.model.region.Region;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public")
@RequestMapping(value = "/public/catalogs")
@RestController
public class CatalogController {

    private final CatalogService catalogService;

    @Autowired
    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Operation(
            summary = "List available catalogs and packages for installing.",
            description =
                    "List available catalogs and packages for installing in the first Region configuration of this Onyxia API.")
    @GetMapping
    public Catalogs getCatalogs(@Parameter(hidden = true) Region region) {
        return catalogService.getCatalogs(region);
    }
}
