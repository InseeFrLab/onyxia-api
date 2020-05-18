package fr.insee.onyxia.api.universe;

import fr.insee.onyxia.api.services.impl.MarathonAppsService;
import fr.insee.onyxia.model.region.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class MarathonAppsServiceTest {

    @ParameterizedTest
    @CsvSource({
            "/users/id/postgres-7227498276193646058,      postgres-7227498276193646058-id-users.dnsSuffix",
            "/users/,     users.dnsSuffix",
            "/, .dnsSuffix"
    })
    public void testGetInternalDns(String id, String response) {
        MarathonAppsService marathonAppsService = new MarathonAppsService();
        Region region = new Region();
        String dnsSuffix = "dnsSuffix";
        region.setMarathonDnsSuffix(dnsSuffix);
        Assertions.assertEquals(marathonAppsService.getInternalDnsFromId(id, region.getMarathonDnsSuffix()), response);
    }
}
