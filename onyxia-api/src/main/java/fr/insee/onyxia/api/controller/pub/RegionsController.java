package fr.insee.onyxia.api.controller.pub;

import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.model.service.Service;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Regions", description = "Regions")
@RequestMapping("/public/regions")
@RestController
public class RegionsController {

    @Autowired
    private RegionsConfiguration regionsConfiguration;

    @GetMapping
    public List<RegionDTO>  getRegions() {
        return regionsConfiguration.getResolvedRegions().stream().map(region -> {
            RegionDTO regionDTO = new RegionDTO();
            regionDTO.setRegionId(region.getRegionId());
            regionDTO.setType(region.getType());
            return regionDTO;
        }).collect(Collectors.toList());
    }


    public static class RegionDTO {

        private String regionId;
        private Service.ServiceType type;

        public String getRegionId() {
            return regionId;
        }

        public void setRegionId(String regionId) {
            this.regionId = regionId;
        }

        public Service.ServiceType getType() {
            return type;
        }

        public void setType(Service.ServiceType type) {
            this.type = type;
        }
    }
}
