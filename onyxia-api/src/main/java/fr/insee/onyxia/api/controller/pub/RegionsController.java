package fr.insee.onyxia.api.controller.pub;

import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.model.region.Region;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Regions", description = "Regions")
@RequestMapping("/public/regions")
@RestController
public class RegionsController {

    @Autowired
    private RegionsConfiguration regionsConfiguration;

    @GetMapping
    public Region[] getRegions() {
        return regionsConfiguration.getResolvedRegions();
    }
}
