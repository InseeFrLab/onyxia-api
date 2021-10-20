package fr.insee.onyxia.model.service.quota;

public class QuotaUsage {

    private Quota spec;
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
