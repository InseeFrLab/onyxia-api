package fr.insee.onyxia.api.controller.pub;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.Catalogs;
import fr.insee.onyxia.api.configuration.NotFoundException;
import fr.insee.onyxia.api.services.CatalogService;
import fr.insee.onyxia.model.catalog.Pkg;
import fr.insee.onyxia.model.helm.Chart;
import fr.insee.onyxia.model.region.Region;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public")
@RequestMapping(value = {"/public/catalog", "/public/catalogs"})
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

    @Operation(
            summary = "List available packages for installing given a catalog.",
            description =
                    "List available packages for installing given a catalog with detailed information on the packages including: descriptions, sources, and configuration options.",
            parameters = {
                @Parameter(
                        required = true,
                        name = "catalogId",
                        description = "Unique ID of the enabled catalog for this Onyxia API.",
                        in = ParameterIn.PATH)
            })
    @GetMapping("{catalogId}")
    public CatalogWrapper getCatalogById(@PathVariable String catalogId) {
        return catalogService.getCatalogById(catalogId).orElseThrow(NotFoundException::new);
    }

    @Operation(
            summary = "Get a service package information from a specific catalog.",
            description =
                    "Get a service package information from a specific catalog, with detailed information on the package including: descriptions, sources, and configuration options.",
            parameters = {
                @Parameter(
                        required = true,
                        name = "catalogId",
                        description = "Unique ID of the enabled catalog for this Onyxia API.",
                        in = ParameterIn.PATH),
                @Parameter(
                        required = true,
                        name = "packageName",
                        description = "Unique name of the package from the selected catalog.",
                        in = ParameterIn.PATH)
            })
    @GetMapping("{catalogId}/{packageName}")
    public Pkg getPackage(@PathVariable String catalogId, @PathVariable String packageName) {
        Pkg pkg =
                catalogService
                        .getPackage(catalogId, packageName)
                        .orElseThrow(NotFoundException::new);
        return pkg;
    }

    @Operation(
            summary = "Get a helm chart from a specific catalog by version.",
            description =
                    "Get a helm chart from a specific catalog by version, with detailed information on the package including: descriptions, sources, and configuration options.",
            parameters = {
                @Parameter(
                        required = true,
                        name = "catalogId",
                        description = "Unique ID of the enabled catalog for this Onyxia API.",
                        in = ParameterIn.PATH),
                @Parameter(
                        required = true,
                        name = "chartName",
                        description = "Unique name of the chart from the selected catalog.",
                        in = ParameterIn.PATH),
                @Parameter(
                        required = true,
                        name = "version",
                        description = "Version of the chart",
                        in = ParameterIn.PATH)
            })
    @GetMapping("{catalogId}/charts/{chartName}/versions/{version}")
    public Chart getChartByVersion(
            @PathVariable String catalogId,
            @PathVariable String chartName,
            @PathVariable String version) {
        Chart chart =
                catalogService
                        .getChartByVersion(catalogId, chartName, version)
                        .orElseThrow(NotFoundException::new);
        return chart;
    }

    @Operation(
            summary = "Get all versions of a chart from a specific catalog.",
            description =
                    "Get all versions of a chart from a specific catalog, with detailed information on the package including: descriptions, sources, and configuration options.",
            parameters = {
                @Parameter(
                        required = true,
                        name = "catalogId",
                        description = "Unique ID of the enabled catalog for this Onyxia API.",
                        in = ParameterIn.PATH),
                @Parameter(
                        required = true,
                        name = "chartName",
                        description = "Unique name of the chart from the selected catalog.",
                        in = ParameterIn.PATH)
            })
    @GetMapping("{catalogId}/charts/{chartName}")
    public List<Chart> getCharts(@PathVariable String catalogId, @PathVariable String chartName) {
        List<Chart> charts =
                catalogService.getCharts(catalogId, chartName).orElseThrow(NotFoundException::new);
        return charts;
    }
}
