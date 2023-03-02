package fr.insee.onyxia.model.service.quota;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "")
public class QuotaUsage {

    @Schema(description = "")
    private Quota spec;

    @Schema(description = "")
    private Quota usage;

    public Quota getSpec() {
        return spec;
    }

    public Quota getUsage() {
        return usage;
    }

    public void setSpec(Quota spec) {
        this.spec = spec;
    }

    public void setUsage(Quota usage) {
        this.usage = usage;
    }
}
