package fr.insee.onyxia.api.controller.pub;

import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.model.region.Region;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name="Public")
@RequestMapping("/public")
public class ConfigurationController {

    @Autowired
    private BuildProperties build;

    @Autowired
    private RegionsConfiguration regionsConfiguration;

    @GetMapping("/configuration")
    public AppInfo configuration() {
        AppInfo appInfo = new AppInfo();
        appInfo.setBuild(build);
        appInfo.setRegions(regionsConfiguration.getResolvedRegions());
        return appInfo;
    }

    public class AppInfo {


        private BuildProperties build;
        private List<Region> regions;


        public void setBuild(BuildProperties build) {
            this.build = build;
        }

        public void setRegions(List<Region> regions) {
            this.regions = regions;
        }

        public BuildProperties getBuild() {
            return build;
        }

        public List<Region> getRegions() {
            return regionsConfiguration.getResolvedRegions();
        }
    }
}
