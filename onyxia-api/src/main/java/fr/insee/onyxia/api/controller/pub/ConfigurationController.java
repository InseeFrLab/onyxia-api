package fr.insee.onyxia.api.controller.pub;

import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.model.region.Region;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name="Public", description = "Information endpoints")
@RequestMapping("/public")
public class ConfigurationController {

    @Autowired(required = false)
    private BuildProperties build;

    @Autowired
    private RegionsConfiguration regionsConfiguration;

    @Operation(
        summary = "Get this Onyxia API full configuration description.",
        description = "Get Onyxia API build info and associated list of Regions, the configuration blocks of Onyxia."
    )
    @GetMapping("/configuration")
    public AppInfo configuration() {
        AppInfo appInfo = new AppInfo();
        BuildInfo buildInfo = new BuildInfo();
        if (build != null) {
            buildInfo.setVersion(build.getVersion());
            buildInfo.setTimestamp(build.getTime().getEpochSecond());
        }
        appInfo.setBuild(buildInfo);
        appInfo.setRegions(regionsConfiguration.getResolvedRegions());
        return appInfo;
    }

    public class AppInfo {


        private BuildInfo build;
        private List<Region> regions;


        public void setBuild(BuildInfo build) {
            this.build = build;
        }

        public void setRegions(List<Region> regions) {
            this.regions = regions;
        }

        public BuildInfo getBuild() {
            return build;
        }

        public List<Region> getRegions() {
            return regionsConfiguration.getResolvedRegions();
        }
    }

    public class BuildInfo {
        private String version;
        private long timestamp;

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getVersion() {
            return version;
        }
    }
}
