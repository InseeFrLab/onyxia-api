package fr.insee.onyxia.api.configuration.checks;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DisableChecksForTests {

    @MockBean CompatibilityChecks compatibilityChecks;

    public CompatibilityChecks getCompatibilityChecks() {
        return compatibilityChecks;
    }

    public void setCompatibilityChecks(CompatibilityChecks compatibilityChecks) {
        this.compatibilityChecks = compatibilityChecks;
    }
}
