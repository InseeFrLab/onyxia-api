package fr.insee.onyxia.api.services.impl.kubernetes;

import fr.insee.onyxia.api.configuration.kubernetes.KubernetesClientProvider;
import fr.insee.onyxia.api.controller.exception.NamespaceNotFoundException;
import fr.insee.onyxia.api.services.impl.HelmAppsService;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.project.Project;
import fr.insee.onyxia.model.region.Region;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KubernetesService {

    private final KubernetesClientProvider kubernetesClientProvider;

    public static final String ONYXIA_QUOTA = "onyxia-quota";

    @Autowired
    public KubernetesService(KubernetesClientProvider kubernetesClientProvider) {
        this.kubernetesClientProvider = kubernetesClientProvider;
    }

    /**
     * Namespaces are created by the onboarding API: a project namespace that does not exist (or has
     * not been onboarded yet) is reported as not found.
     */
    @NotNull
    public String determineNamespace(Region region, Project project, User user) {
        if (region.getServices().isSingleNamespace()) {
            return getCurrentNamespace(region);
        }
        if (StringUtils.isEmpty(project.getNamespace())) {
            throw new NamespaceNotFoundException();
        }
        if (!isNamespaceAlreadyExisting(region, user, project.getNamespace())) {
            throw new NamespaceNotFoundException();
        }
        return project.getNamespace();
    }

    public String getCurrentNamespace(Region region) {
        return kubernetesClientProvider.getRootClient(region).getNamespace();
    }

    private boolean isNamespaceAlreadyExisting(Region region, User user, String namespaceId) {
        if (isTargetNamespaceServiceAccountMode(region)) {
            // The namespace service account can read its own namespace. If the namespace does not
            // exist or has not been onboarded yet, it has no permission there: 403, not 404.
            try {
                return kubernetesClientProvider
                                .getUserClient(region, user, namespaceId)
                                .namespaces()
                                .withName(namespaceId)
                                .get()
                        != null;
            } catch (KubernetesClientException e) {
                if (e.getCode() == 403) {
                    return false;
                }
                throw e;
            }
        }
        return kubernetesClientProvider
                        .getRootClient(region)
                        .namespaces()
                        .withName(namespaceId)
                        .get()
                != null;
    }

    private boolean isTargetNamespaceServiceAccountMode(Region region) {
        return region.getServices().getAuthenticationMode()
                == Region.Services.AuthenticationMode.SERVICEACCOUNTINTARGETNAMESPACE;
    }

    /**
     * Client for calls on a user namespace that do not depend on the user permissions: the
     * namespace service account in SERVICEACCOUNTINTARGETNAMESPACE mode, the root client otherwise.
     */
    private KubernetesClient getNamespaceClient(Region region, User user, String namespace) {
        if (isTargetNamespaceServiceAccountMode(region)) {
            return kubernetesClientProvider.getUserClient(region, user, namespace);
        }
        return kubernetesClientProvider.getRootClient(region);
    }

    public void createOnyxiaSecret(
            Region region,
            User user,
            String namespaceId,
            String releaseName,
            Map<String, String> secretData) {

        final KubernetesClient kubClient = getNamespaceClient(region, user, namespaceId);
        String ownerSecretName =
                "sh.helm.release.v1." + releaseName + ".v1"; // Name of the Secret managed by Helm

        // Fetch the existing secret managed by Helm
        Secret ownerSecret =
                kubClient.secrets().inNamespace(namespaceId).withName(ownerSecretName).get();

        // Create owner reference
        OwnerReference ownerReference =
                new OwnerReferenceBuilder()
                        .withApiVersion(ownerSecret.getApiVersion())
                        .withKind(ownerSecret.getKind())
                        .withName(ownerSecret.getMetadata().getName())
                        .withUid(ownerSecret.getMetadata().getUid())
                        .withController(true) // Optional: Specifies that this owner is a controller
                        .build();

        // Define the new secret
        Secret newSecret =
                new SecretBuilder()
                        .withNewMetadata()
                        .withName(HelmAppsService.ONYXIA_SECRET_PREFIX + releaseName)
                        .addNewOwnerReferenceLike(ownerReference)
                        .endOwnerReference()
                        .endMetadata()
                        .addToData(secretData)
                        .withType("onyxia.sh/release.v1")
                        .build();

        // Create the secret in Kubernetes
        newSecret = kubClient.secrets().inNamespace(namespaceId).resource(newSecret).create();
    }

    public ResourceQuota getOnyxiaQuota(Region region, Project project, User user) {
        final String namespace = determineNamespace(region, project, user);
        final KubernetesClient kubClient = getNamespaceClient(region, user, namespace);
        return kubClient.resourceQuotas().inNamespace(namespace).withName(ONYXIA_QUOTA).get();
    }

    public static class Owner {
        private String id;
        private OwnerType type;

        public static enum OwnerType {
            USER,
            GROUP;
        }

        public OwnerType getType() {
            return type;
        }

        public void setType(OwnerType type) {
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
