package fr.insee.onyxia.model.region;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;

import fr.insee.onyxia.model.region.Server.Auth;

public class ServerSerializerTest {

    ObjectMapper objectMapper = new ObjectMapper();
    Server server;

    @BeforeEach
    public void setUp() {
        server = new Server();
        server.setPrivateUrl("hidden.url.fr");
        server.setPublicUrl("public.url.fr");
        Auth auth = new Auth();
        auth.setPassword("this password should never be displayed");
        server.setAuth(auth);
        KeycloakParams keycloakParams = new KeycloakParams();
        keycloakParams.setRealm("thisrealmisdisplayed");
        server.setKeycloakParams(keycloakParams);
    }

    @Test
    public void serverSerializeTest() throws Exception {
        Server res = objectMapper.readValue(objectMapper.writeValueAsString(server), Server.class);
        assertThat("Private url should not be displayed", res.getPrivateUrl() == null);
        assertThat("Public url should be displayed", res.getPublicUrl().equals("public.url.fr"));
        assertThat("Auth data should never be displayed", res.getAuth() == null);
        assertThat("Keycloak params should be displayed",
                res.getKeycloakParams().getRealm().equals("thisrealmisdisplayed"));
    }
}
