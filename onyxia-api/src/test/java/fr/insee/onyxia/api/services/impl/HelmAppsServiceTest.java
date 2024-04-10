package fr.insee.onyxia.api.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class HelmAppsServiceTest {
    @Test
    void shouldParseHelmDates() throws Exception {
        assertEquals(
                1712677329000L,
                HelmAppsService.HELM_DATE_FORMAT.parse("Tue Apr  9 17:42:09 2024").getTime());
    }
}
