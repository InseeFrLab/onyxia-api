package fr.insee.onyxia.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Parameter for service upgrade (re-deploying an existing service)")
public class UpgradeServiceDTO {
    @Schema(
            description =
                    "Version of the helm package to upgrade to. If null, the service is upgraded with its current chart version.",
            required = false)
    String packageVersion;

    @Schema(description = "Content of values.yaml", required = true)
    Object options;

    @Schema(
            description =
                    "When true, all users of the namespace will list this service. If null, the current value is kept.",
            required = false)
    Boolean share;

    @Schema(
            description = "A friendly name for the service. If null, the current value is kept.",
            required = false)
    String friendlyName;

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public Object getOptions() {
        return options;
    }

    public void setOptions(Object options) {
        this.options = options;
    }

    public Boolean getShare() {
        return share;
    }

    public void setShare(Boolean share) {
        this.share = share;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }
}
