package fr.insee.onyxia.api.configuration.properties;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "health.custom-crd")
public class CrdHealthCheckProperties {

    private List<CrdHealthCheck> checks = new ArrayList<>();

    public List<CrdHealthCheck> getChecks() {
        return checks;
    }

    public void setChecks(List<CrdHealthCheck> checks) {
        this.checks = checks;
    }

    public enum Strategy {
        FIELDS,
        CONDITION
    }

    public static class CrdHealthCheck {
        private String group;
        private String version;
        private String plural;
        private String kind;
        private Strategy strategy = Strategy.FIELDS;
        private String desiredField;
        private String readyField;
        private String conditionType = "Ready";

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getPlural() {
            return plural;
        }

        public void setPlural(String plural) {
            this.plural = plural;
        }

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public Strategy getStrategy() {
            return strategy;
        }

        public void setStrategy(Strategy strategy) {
            this.strategy = strategy;
        }

        public String getDesiredField() {
            return desiredField;
        }

        public void setDesiredField(String desiredField) {
            this.desiredField = desiredField;
        }

        public String getReadyField() {
            return readyField;
        }

        public void setReadyField(String readyField) {
            this.readyField = readyField;
        }

        public String getConditionType() {
            return conditionType;
        }

        public void setConditionType(String conditionType) {
            this.conditionType = conditionType;
        }
    }
}
