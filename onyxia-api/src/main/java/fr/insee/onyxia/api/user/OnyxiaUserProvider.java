package fr.insee.onyxia.api.user;

import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.impl.kubernetes.KubernetesService;
import fr.insee.onyxia.model.OnyxiaUser;
import fr.insee.onyxia.model.project.Project;
import fr.insee.onyxia.model.region.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OnyxiaUserProvider {


    @Autowired
    private UserProvider userProvider;

    @Autowired
    private RegionsConfiguration regionsConfiguration;

    @Autowired
    private KubernetesService kubernetesService; // TODO : cleanup

    public OnyxiaUser getUser() {
        Region region = regionsConfiguration.getDefaultRegion();
        OnyxiaUser user = new OnyxiaUser(userProvider.getUser());

        Project userProject = getUserProject(region, user);
        user.getProjects().add(userProject);

        userProvider.getUser().getGroups().stream().forEach(group -> {
            Project project = new Project();
            project.setId(region.getServices().getGroupNamespacePrefix()+group);
            project.setGroup(group);
            project.setBucket(region.getServices().getGroupNamespacePrefix()+group);
            project.setNamespace(region.getServices().getGroupNamespacePrefix()+group);
            user.getProjects().add(project);
        });

        return user;
    }

    private Project getUserProject(Region region, OnyxiaUser user ) {
        Project userProject = new Project();
        if (region.getServices().isSingleNamespace()) {
            userProject.setId("single-project");
            userProject.setGroup(null);
            userProject.setBucket(region.getServices().getNamespacePrefix()+user.getUser().getIdep());
            userProject.setNamespace(kubernetesService.getCurrentNamespace(region));
            userProject.setName("Single namespace, single project");
        }
        else {
            userProject.setId(region.getServices().getNamespacePrefix()+user.getUser().getIdep());
            userProject.setGroup(null);
            userProject.setBucket(region.getServices().getNamespacePrefix()+user.getUser().getIdep());
            userProject.setNamespace(region.getServices().getNamespacePrefix()+user.getUser().getIdep());
            userProject.setName(user.getUser().getIdep()+" personal project");
        }
        return userProject;
    }
}
