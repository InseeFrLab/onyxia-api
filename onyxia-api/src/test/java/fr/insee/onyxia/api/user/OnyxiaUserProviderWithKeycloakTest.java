package fr.insee.onyxia.api.user;

import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.api.security.KeycloakUserProvider;
import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.impl.kubernetes.KubernetesService;
import fr.insee.onyxia.api.services.utils.HttpRequestUtils;
import fr.insee.onyxia.model.OnyxiaUser;
import fr.insee.onyxia.model.region.Region;
import fr.insee.onyxia.model.region.Region.Services;

import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.any;

import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = { OnyxiaUserProvider.class, UserProvider.class, KeycloakUserProvider.class })
@TestPropertySource("classpath:application-keycloak.properties")
public class OnyxiaUserProviderWithKeycloakTest {

   @Autowired
   OnyxiaUserProvider onyxiaUserProvider;

   @MockBean
   private RegionsConfiguration regionsConfiguration;

   @MockBean
   private KubernetesService kubernetesService;

   @MockBean
   private HttpRequestUtils httpRequestUtils;

   @MockBean
   AccessToken accessToken;
   
   @Autowired GenericApplicationContext context;

   Region region;

   @BeforeEach
   public void setUp() {
      context.registerBean("getAccessTokenString", String.class, () -> "fakeToken");
      region = new Region();
      Services servicesConfiguration = new Services();
      servicesConfiguration.setSingleNamespace(false);
      region.setServices(servicesConfiguration);
      when(regionsConfiguration.getDefaultRegion()).thenReturn(region);
      when(httpRequestUtils.getClientIpAddressIfServletRequestExist(any())).thenReturn("");
   }

   @DisplayName("Given a multi namespace region with no group pattern set, " +
         "when we ask for the user groups, " +
         "then the user should have all groups coming from the access token")
   @Test
   public void shouldReturnGroupsFromAccessToken() {
      setGroupsInAccessTokenTo(List.of("group1", "group2_Onyxia", "877@_Onyxia"));
      OnyxiaUser simpleUser = onyxiaUserProvider.getUser(region);
      assertGroupBelongsToUser(simpleUser, "group1");
      assertGroupBelongsToUser(simpleUser, "group2_Onyxia");
   }

   @DisplayName("Given a multi namespace region with a group pattern set, " +
         "when we ask for the user groups, " +
         "then the user should only get the access token matching groups")
   @Test
   public void shouldOnlyReturnGroupMatchingWhenRegionRule() {
      setGroupsInAccessTokenTo(List.of("group1", "group2_Onyxia", "877@_Onyxia"));
      region.setIncludedGroupPattern(".*_Onyxia");
      OnyxiaUser simpleUser = onyxiaUserProvider.getUser(region);
      assertGroupDoesntBelongToUser(simpleUser, "group1");
      assertGroupBelongsToUser(simpleUser, "group2_Onyxia");
      assertGroupBelongsToUser(simpleUser, "877@_Onyxia");
   }

   @DisplayName("Given a multi namespace region with a group pattern to exclude set, " +
         "when we ask for the user groups, " +
         "then the user should not get the access token matching the exclude pattern")
   @Test
   public void shouldNotReturnGroupMatchingExcludePatternWhenRegionRule() {
      setGroupsInAccessTokenTo(List.of("group1", "group2_Onyxia", "877@_Onyxia"));
      region.setExcludedGroupPattern("group2_Onyxia");
      OnyxiaUser simpleUser = onyxiaUserProvider.getUser(region);
      assertGroupBelongsToUser(simpleUser, "group1");
      assertGroupDoesntBelongToUser(simpleUser, "group2_Onyxia");
      assertGroupBelongsToUser(simpleUser, "877@_Onyxia");
   }

   @DisplayName("Given a multi namespace region and a user that has no group claims, " +
         "when we ask for the user groups, " +
         "then the user should have no group set")
   @Test
   public void noGroupsClaimsInAccessTokenShouldReturnEmptyGroups() {
      Map<String, Object> otherClaims = Map.of();
      when(accessToken.getOtherClaims()).thenReturn(otherClaims);
      assertThat("The user doesn't have group", onyxiaUserProvider.getUser(region).getUser().getGroups().isEmpty());

   }

   @DisplayName("Given a multi namespace region and a user that has an empty group claim, " +
         "when we ask for the user groups, " +
         "then the user should have no group set")
   @Test
   public void emptyGroupsInAccessTokenShouldReturnEmptyGroups() {
      setGroupsInAccessTokenTo(List.of());
      assertThat("The user doesn't have group", onyxiaUserProvider.getUser(region).getUser().getGroups().isEmpty());
   }

   private void assertGroupBelongsToUser(OnyxiaUser user, String testedGroup) {
      assertThat(testedGroup + " belongs to groups", user.getUser().getGroups().contains(testedGroup));
   }

   private void assertGroupDoesntBelongToUser(OnyxiaUser user, String testedGroup) {
      assertThat(testedGroup + " does not belong to groups", !user.getUser().getGroups().contains(testedGroup));
   }

   private void setGroupsInAccessTokenTo(List<String> groups) {
      Map<String, Object> otherClaims = Map.of("groups", groups);
      when(accessToken.getOtherClaims()).thenReturn(otherClaims);
   }
}
