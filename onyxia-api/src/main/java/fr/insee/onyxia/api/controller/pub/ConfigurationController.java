package fr.insee.onyxia.api.controller.pub;

import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.api.security.OIDCConfiguration;
import fr.insee.onyxia.model.region.Region;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Public", description = "Information endpoints")
@RequestMapping("/public")
public class ConfigurationController {

    @Autowired(required = false)
    private BuildProperties build;

    @Autowired(required = false)
    private OIDCConfiguration oidcConfiguration;

    @Autowired private RegionsConfiguration regionsConfiguration;

    @Operation(
            summary = "Get this Onyxia API full configuration description.",
            description =
                    "Get Onyxia API build info and associated list of Regions, the configuration blocks of Onyxia.")
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
        AuthenticationInfo authenticationInfo = new AuthenticationInfo();
        if (oidcConfiguration != null) {
            authenticationInfo.setMode("openidconnect");
            OpenIDConnectConfigurationInfo oidcInfo = new OpenIDConnectConfigurationInfo();
            oidcInfo.setClientID(oidcConfiguration.getClientID());
            oidcInfo.setIssuerURI(oidcConfiguration.getIssuerUri());
            oidcInfo.setAudience(oidcConfiguration.getAudience());
            authenticationInfo.setOidcConfiguration(oidcInfo);
        }
        appInfo.setAuthenticationInfo(authenticationInfo);
        return appInfo;
    }

    public OIDCConfiguration getOidcConfiguration() {
        return oidcConfiguration;
    }

    public void setOidcConfiguration(OIDCConfiguration oidcConfiguration) {
        this.oidcConfiguration = oidcConfiguration;
    }

    public static class AuthenticationInfo {
        private String mode = "none";
        private OpenIDConnectConfigurationInfo oidcConfiguration = null;

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public OpenIDConnectConfigurationInfo getOidcConfiguration() {
            return oidcConfiguration;
        }

        public void setOidcConfiguration(OpenIDConnectConfigurationInfo oidcConfiguration) {
            this.oidcConfiguration = oidcConfiguration;
        }
    }

    public static class OpenIDConnectConfigurationInfo {
        private String issuerURI;
        private String clientID;
        private String audience = null;

        public String getIssuerURI() {
            return issuerURI;
        }

        public void setIssuerURI(String issuerURI) {
            this.issuerURI = issuerURI;
        }

        public String getClientID() {
            return clientID;
        }

        public void setClientID(String clientID) {
            this.clientID = clientID;
        }

        public String getAudience() {
            return audience;
        }

        public void setAudience(String audience) {
            this.audience = audience;
        }
    }

    @Schema(description = "Cloudshell data and health")
    public class AppInfo {

        private BuildInfo build;
        private List<Region> regions;

        private AuthenticationInfo authenticationInfo;

        public BuildInfo getBuild() {
            return build;
        }

        public void setBuild(BuildInfo build) {
            this.build = build;
        }

        public List<Region> getRegions() {
            return regionsConfiguration.getResolvedRegions();
        }

        public void setRegions(List<Region> regions) {
            this.regions = regions;
        }

        public AuthenticationInfo getAuthenticationInfo() {
            return authenticationInfo;
        }

        public void setAuthenticationInfo(AuthenticationInfo authenticationInfo) {
            this.authenticationInfo = authenticationInfo;
        }
    }

    @Schema(description = "")
    public class BuildInfo {
        @Schema(description = "")
        private String version;

        @Schema(description = "")
        private long timestamp;

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
}
