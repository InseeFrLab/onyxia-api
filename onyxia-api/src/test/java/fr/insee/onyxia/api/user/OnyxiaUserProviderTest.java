package fr.insee.onyxia.api.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.impl.kubernetes.KubernetesService;
import fr.insee.onyxia.model.OnyxiaUser;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.project.Project;
import fr.insee.onyxia.model.region.Region;
import fr.insee.onyxia.model.region.Region.Services;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(classes = {OnyxiaUserProvider.class, UserProvider.class})
public class OnyxiaUserProviderTest {

    @Autowired OnyxiaUserProvider onyxiaUserProvider;

    @MockBean private KubernetesService kubernetesService;

    @MockBean private UserProvider userProvider;

    Region region;
    User user;

    @BeforeEach
    public void setUp() {
        region = new Region();
        user = new User();
        Services servicesConfiguration = new Services();
        servicesConfiguration.setSingleNamespace(false);
        region.setServices(servicesConfiguration);
        when(userProvider.getUser(any())).thenReturn(user);
    }

    @DisplayName(
            "Given a multi namespace region with a group pattern set "
                    + "and group transformation enabled, when we ask for an invalid transformation, "
                    + "then the user should not have group projects")
    @Test
    public void shouldNotHaveProjectAddedWhenConfWrong() {
        region.setIncludedGroupPattern("(.*)-onyxia");
        region.setTransformGroupPattern("$2");
        user.setGroups(List.of("group2-onyxia"));
        OnyxiaUser simpleUser = onyxiaUserProvider.getUser(region);
        assertGroupDoesntBelongToUserProjects(simpleUser, "group2-onyxia");
    }

    @Test
    public void namespaceNotRespectingRFC1123ShouldBeRejected() {
        user.setGroups(
                List.of(
                        "group1",
                        "toto_onyxia",
                        "titi-Onyxia",
                        "-titi",
                        "morethan64chargroupmorethan64chargroupmorethan64chargroupmorethan",
                        "groupwith@"));
        OnyxiaUser simpleUser = onyxiaUserProvider.getUser(region);
        assertGroupBelongsToUserProjects(simpleUser, "group1");
        assertGroupDoesntBelongToUserProjects(simpleUser, "titi-Onyxia");
        assertGroupBelongsToUserProjects(simpleUser, "-titi");
        assertGroupDoesntBelongToUserProjects(simpleUser, "group@with");
        assertGroupDoesntBelongToUserProjects(simpleUser, "toto_onyxia");
        assertGroupDoesntBelongToUserProjects(
                simpleUser, "morethan64chargroupmorethan64chargroupmorethan64chargroupmorethan");
    }

    @DisplayName(
            "Given a multi namespace region with a group pattern set "
                    + "and group transformation enabled, when we ask for the user groups, "
                    + "then the user should get a project for transformed groups")
    @Test
    public void shouldHaveAProjectWithTransformedNamespace() {
        region.setIncludedGroupPattern("(.*)_Onyxia");
        region.setTransformGroupPattern("$1-k8s");
        user.setGroups(List.of("group2_Onyxia"));
        OnyxiaUser simpleUser = onyxiaUserProvider.getUser(region);
        assertThat("The user should have 2 projects", simpleUser.getProjects().size(), is(2));
        assertThat(
                "One of this project is personal, meaning it is not associated to a group",
                simpleUser.getProjects().stream().anyMatch(p -> p.getGroup() == null));
        Project group2Project =
                simpleUser.getProjects().stream()
                        .filter(p -> p.getGroup() == "group2_Onyxia")
                        .findFirst()
                        .get();
        assertThat(
                "The group project should have a transformed namespace",
                group2Project.getNamespace(),
                is("projet-group2-k8s"));
    }

    private void assertGroupBelongsToUserProjects(OnyxiaUser user, String testedGroup) {
        assertThat(
                testedGroup + " should belong to user projects",
                user.getProjects().stream()
                        .anyMatch(p -> p.getGroup() != null && p.getGroup().equals(testedGroup)));
    }

    private void assertGroupDoesntBelongToUserProjects(OnyxiaUser user, String testedGroup) {
        assertThat(
                testedGroup + " should not belong to user projects",
                user.getProjects().stream()
                        .noneMatch(p -> p.getGroup() != null && p.getGroup().equals(testedGroup)));
    }
}
