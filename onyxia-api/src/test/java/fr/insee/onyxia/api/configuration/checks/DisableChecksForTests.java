package fr.insee.onyxia.api.configuration.checks;

import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Configuration
public class DisableChecksForTests {

    @MockitoBean CompatibilityChecks compatibilityChecks;

    public CompatibilityChecks getCompatibilityChecks() {
        return compatibilityChecks;
    }

    public void setCompatibilityChecks(CompatibilityChecks compatibilityChecks) {
        this.compatibilityChecks = compatibilityChecks;
    }
}
