package fr.insee.onyxia.api.controller.pub;

import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.model.region.Region;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Public")
@RequestMapping("/public/regions")
@RestController
public class RegionsController {

    @Autowired
    private RegionsConfiguration regionsConfiguration;

    @Operation(
        summary = "Get Onyxia API regions configurations.",
        description = "Get Onyxia API associated list of Regions, the configuration blocks of Onyxia. Recall that the first region will be used as default for /my-lab/ requests."
    )
    @GetMapping
    public List<Region>  getRegions() {
        return regionsConfiguration.getResolvedRegions();
    }
}
