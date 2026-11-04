package fr.insee.onyxia.api.configuration.kubernetes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.region.Region;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.jupiter.api.Test;

class KubernetesClientProviderTest {

    private final KubernetesClientProvider provider = new KubernetesClientProvider();

    private Region region(Region.Services.AuthenticationMode authenticationMode) {
        Region region = new Region();
        region.setId("test");
        region.getServices().setAuthenticationMode(authenticationMode);
        return region;
    }

    @Test
    void shouldImpersonateTheTargetNamespaceServiceAccount() {
        Region region = region(Region.Services.AuthenticationMode.SERVICEACCOUNTINTARGETNAMESPACE);

        try (KubernetesClient client = provider.getUserClient(region, new User(), "user-jdoe")) {
            assertEquals(
                    "system:serviceaccount:user-jdoe:onyxia-local",
                    client.getConfiguration().getImpersonateUsername());
        }
    }

    @Test
    void shouldImpersonateTheConfiguredServiceAccount() {
        Region region = region(Region.Services.AuthenticationMode.SERVICEACCOUNTINTARGETNAMESPACE);
        region.getServices().setTargetNamespaceServiceAccount("onyxia-installer");

        try (KubernetesClient client = provider.getUserClient(region, new User(), "user-jdoe")) {
            assertEquals(
                    "system:serviceaccount:user-jdoe:onyxia-installer",
                    client.getConfiguration().getImpersonateUsername());
        }
    }

    @Test
    void shouldRequireANamespaceToImpersonateItsServiceAccount() {
        Region region = region(Region.Services.AuthenticationMode.SERVICEACCOUNTINTARGETNAMESPACE);

        assertThrows(
                IllegalArgumentException.class,
                () -> provider.getUserClient(region, new User(), null));
    }

    @Test
    void shouldNotImpersonateInServiceAccountMode() {
        Region region = region(Region.Services.AuthenticationMode.SERVICEACCOUNT);

        KubernetesClient client = provider.getUserClient(region, new User(), "user-jdoe");
        assertNull(client.getConfiguration().getImpersonateUsername());
    }
}
