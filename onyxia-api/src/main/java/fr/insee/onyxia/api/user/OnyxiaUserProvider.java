package fr.insee.onyxia.api.user;

import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.impl.kubernetes.KubernetesService;
import fr.insee.onyxia.model.OnyxiaUser;
import fr.insee.onyxia.model.project.Project;
import fr.insee.onyxia.model.region.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class OnyxiaUserProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnyxiaUserProvider.class);

    Pattern rfc1123Pattern = Pattern.compile("[a-z0-9]([-a-z0-9]*[a-z0-9])?");
    private final UserProvider userProvider;
    private final KubernetesService kubernetesService; // TODO : cleanup

    @Autowired
    public OnyxiaUserProvider(UserProvider userProvider, KubernetesService kubernetesService) {
        this.userProvider = userProvider;
        this.kubernetesService = kubernetesService;
    }

    public OnyxiaUser getUser(Region region) {
        OnyxiaUser user = new OnyxiaUser(userProvider.getUser(region));

        Project userProject = getUserProject(region, user);
        user.getProjects().add(userProject);

        Pattern includedGroupPattern = getPrecompiledIncludedGroupPattern(region);

        if (!region.getServices().isSingleNamespace()) {
            userProvider
                    .getUser(region)
                    .getGroups()
                    .forEach(
                            group -> {
                                String projectBaseName =
                                        getProjectBaseNameFromGroup(
                                                group,
                                                includedGroupPattern,
                                                region.getTransformGroupPattern());
                                if (projectBaseName != null) {
                                    Project project = new Project();
                                    project.setGroup(group);
                                    project.setId(
                                            region.getServices().getGroupNamespacePrefix()
                                                    + projectBaseName);
                                    String vaultPrefix = "";
                                    if (region.getVault() != null && region.getVault().getGroupPrefix() != null) {
                                        vaultPrefix = region.getVault().getGroupPrefix();
                                    }
                                    project.setVaultTopDir(vaultPrefix+
                                            region.getServices().getGroupNamespacePrefix()
                                                    + projectBaseName);
                                    project.setNamespace(
                                            region.getServices().getGroupNamespacePrefix()
                                                    + projectBaseName);
                                    user.getProjects().add(project);
                                }
                            });
        }

        user.getProjects().removeIf(p -> !isRespectingRFC1123LabelName(p.getNamespace()));

        return user;
    }

    private Project getUserProject(Region region, OnyxiaUser user) {
        Project userProject = new Project();
        String vaultPrefix = "";
        if (region.getVault() != null && region.getVault().getGroupPrefix() != null) {
            vaultPrefix = region.getVault().getGroupPrefix();
        }
        if (region.getServices().isSingleNamespace()) {
            userProject.setId("single-project");
            userProject.setGroup(null);
            userProject.setVaultTopDir(vaultPrefix+user.getUser().getIdep());
            userProject.setNamespace(kubernetesService.getCurrentNamespace(region));
            userProject.setName("Single namespace, single project");
        } else {
            userProject.setId(region.getServices().getNamespacePrefix() + user.getUser().getIdep());
            userProject.setVaultTopDir(vaultPrefix+user.getUser().getIdep());
            userProject.setGroup(null);
            userProject.setName(user.getUser().getIdep() + " personal project");
            if (region.getServices().isUserNamespace()) {
                userProject.setNamespace(
                        region.getServices().getNamespacePrefix() + user.getUser().getIdep());
            }
        }
        return userProject;
    }

    private Pattern getPrecompiledIncludedGroupPattern(Region region) {
        if (region.getIncludedGroupPattern() != null && region.getTransformGroupPattern() != null) {
            return Pattern.compile(region.getIncludedGroupPattern());
        } else return null;
    }

    private String getProjectBaseNameFromGroup(
            String group, Pattern includePattern, String extractPattern) {
        if (includePattern != null) {
            try {
                return includePattern.matcher(group).replaceAll(extractPattern);
            } catch (Exception e) {
                LOGGER.error(
                        "Failed to transform group project with include pattern : {} "
                                + "and transform pattern : {} . Returning non transformed group.",
                        includePattern.pattern(),
                        extractPattern);
                return null;
            }
        } else return group;
    }

    private boolean isRespectingRFC1123LabelName(String string) {
        return string != null && string.length() <= 63 && rfc1123Pattern.matcher(string).matches();
    }
}
