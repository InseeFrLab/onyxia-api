package fr.insee.onyxia.model.service;

public class HealthCheckResult {
    private boolean healthy;
    private String name;
    private String kind;
    private HealthDetails details;

    public HealthCheckResult() {}

    public HealthCheckResult(boolean healthy, String name, String kind, HealthDetails details) {
        this.healthy = healthy;
        this.name = name;
        this.kind = kind;
        this.details = details;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public HealthDetails getDetails() {
        return details;
    }

    public void setDetails(HealthDetails details) {
        this.details = details;
    }

    public static class HealthDetails {
        private int nbWanted;
        private int nbUp;

        public int getNbUp() {
            return nbUp;
        }

        public int getNbWanted() {
            return nbWanted;
        }

        public void setNbUp(int nbUp) {
            this.nbUp = nbUp;
        }

        public void setNbWanted(int nbWanted) {
            this.nbWanted = nbWanted;
        }
    }
}
