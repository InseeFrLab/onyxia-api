package fr.insee.onyxia.api.user;

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
    private KubernetesService kubernetesService; // TODO : cleanup

    public OnyxiaUser getUser(Region region) {
        OnyxiaUser user = new OnyxiaUser(userProvider.getUser(region));

        Project userProject = getUserProject(region, user);
        user.getProjects().add(userProject);

        if (!region.getServices().isSingleNamespace()) {
            userProvider.getUser(region).getGroups().stream().forEach(group -> {
                Project project = new Project();
                project.setId(region.getServices().getGroupNamespacePrefix()+group);
                project.setGroup(group);
                project.setVaultTopDir(region.getServices().getGroupNamespacePrefix()+group);
                if(region.getData()!=null && region.getData().getS3()!=null){
                    project.setBucket(region.getData().getS3().getGroupBucketPrefix()+group);
                }
                project.setNamespace(region.getServices().getGroupNamespacePrefix()+group);
                user.getProjects().add(project);
            });
        }

        return user;
    }

    private Project getUserProject(Region region, OnyxiaUser user ) {
        Project userProject = new Project();
        if (region.getServices().isSingleNamespace()) {
            userProject.setId("single-project");
            userProject.setGroup(null);
            userProject.setVaultTopDir(user.getUser().getIdep());
            if(region.getData()!=null && region.getData().getS3()!=null){
                userProject.setBucket(region.getData().getS3().getBucketPrefix()+user.getUser().getAttributes().get(region.getData().getS3().getBucketClaim()));
            }
            userProject.setNamespace(kubernetesService.getCurrentNamespace(region));
            userProject.setName("Single namespace, single project");
        }
        else {
            userProject.setId(region.getServices().getNamespacePrefix()+user.getUser().getIdep());
            userProject.setGroup(null);
            userProject.setVaultTopDir(user.getUser().getIdep());
            if(region.getData()!=null && region.getData().getS3()!=null){
                userProject.setBucket(region.getData().getS3().getBucketPrefix()+user.getUser().getAttributes().get(region.getData().getS3().getBucketClaim()));
            }
            userProject.setNamespace(region.getServices().getNamespacePrefix()+user.getUser().getIdep());
            userProject.setName(user.getUser().getIdep()+" personal project");
        }
        return userProject;
    }
}
