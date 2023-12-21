package fr.insee.onyxia.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Paramètre de création d'un service")
public class CreateServiceDTO {
    @Schema(
            description = "Catalog where the package of the service is taken from.",
            required = false,
            defaultValue = "internal")
    String catalogId;

    @Schema(
            description =
                    "Helm package that will be used to create the service, sert à récupérer le package (vérif package)",
            required = true)
    String packageName;

    @Schema(
            description =
                    "Version of the helm package, passe la version dans la création, si pas null",
            required = false)
    String packageVersion;

    @Schema(
            description = "A chosen name for the service, si null un nom est généré par helm",
            required = false)
    String name;

    @Schema(description = "Contenu du values.yaml", required = true)
    Object options;

    @Schema(
            description = "when true nothing is run (mode dry run de helm) faux par défaut",
            required = false,
            defaultValue = "false")
    boolean dryRun = false;

    public String getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(String catalogId) {
        this.catalogId = catalogId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getOptions() {
        return options;
    }

    public void setOptions(Object options) {
        this.options = options;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }
}
