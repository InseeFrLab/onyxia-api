package fr.insee.onyxia.api.controller.api.onboarding;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.onyxia.api.configuration.BaseTest;
import fr.insee.onyxia.api.configuration.SecurityConfig;
import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.impl.kubernetes.KubernetesService;
import fr.insee.onyxia.api.services.utils.HttpRequestUtils;
import fr.insee.onyxia.api.user.OnyxiaUserProvider;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.region.Region;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OnboardingController.class)
class OnboardingControllerTest extends BaseTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private UserProvider userProvider;
    @MockBean private RegionsConfiguration regionsConfiguration;

    @MockBean private HttpRequestUtils httpRequestUtils;
    @MockBean private SecurityConfig securityConfig;
    @MockBean private OnyxiaUserProvider onyxiaUserProvider;
    @MockBean private KubernetesService kubernetesService;

    @Test
    public void should_not_create_namespace_when_single_project() throws Exception {
        Region region = new Region();
        Region.Services servicesConfiguration = new Region.Services();
        servicesConfiguration.setSingleNamespace(true);
        region.setServices(servicesConfiguration);
        when(regionsConfiguration.getDefaultRegion()).thenReturn(region);
        mockMvc.perform(post("/onboarding").content("{}").contentType(APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void should_not_create_namespace_when_allow_namespace_creation_is_false()
            throws Exception {
        Region region = new Region();
        Region.Services servicesConfiguration = new Region.Services();
        servicesConfiguration.setSingleNamespace(false);
        servicesConfiguration.setAllowNamespaceCreation(false);
        region.setServices(servicesConfiguration);
        when(regionsConfiguration.getDefaultRegion()).thenReturn(region);
        mockMvc.perform(post("/onboarding").content("{}").contentType(APPLICATION_JSON))
                .andExpect(status().isNotImplemented());
    }

    @Test
    public void should_create_namespace() throws Exception {
        Region region = new Region();
        Region.Services servicesConfiguration = new Region.Services();
        servicesConfiguration.setSingleNamespace(false);
        servicesConfiguration.setAllowNamespaceCreation(true);
        region.setServices(servicesConfiguration);
        when(regionsConfiguration.getDefaultRegion()).thenReturn(region);
        when(userProvider.getUser(any())).thenReturn(User.newInstance().setIdep("default").build());
        mockMvc.perform(post("/onboarding").content("{}").contentType(APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
