package fr.insee.onyxia.api.services.impl.kubernetes;

import fr.insee.onyxia.api.configuration.kubernetes.KubernetesClientProvider;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.project.Project;
import fr.insee.onyxia.model.region.Region;
import fr.insee.onyxia.model.service.quota.Quota;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.rbac.RoleBinding;
import io.fabric8.kubernetes.api.model.rbac.RoleBindingBuilder;
import io.fabric8.kubernetes.api.model.rbac.SubjectBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class KubernetesService {

    @Autowired
    private KubernetesClientProvider kubernetesClientProvider;

    public String createDefaultNamespace(Region region, Owner owner) {
        String namespaceId = getDefaultNamespace(region, owner);
        return createNamespace(region, namespaceId, owner);
    }

    @NotNull
    public String determineNamespaceAndCreateIfNeeded(Region region, Project project, User user) {
        if (region.getServices().isSingleNamespace()) {
            return getCurrentNamespace(region);
        }
        if (project.getGroup() != null) {
            // For groups, onboarding is done separatly
            return project.getNamespace();
        }
        // TODO : in the future, user onboarding may be done separatly as well
        KubernetesService.Owner owner = new KubernetesService.Owner();
        owner.setId(user.getIdep());
        owner.setType(KubernetesService.Owner.OwnerType.USER);
        return createNamespace(region, project.getNamespace(), owner);
    }

    public String getCurrentNamespace(Region region) {
        return kubernetesClientProvider.getRootClient(region).getNamespace();
    }

    public String createNamespace(Region region, String namespaceId, Owner owner) {
        String name = getNameFromOwner(region, owner);

        // Label onyxia_owner is not resilient if the user has "namespace admin" role scoped to his namespace
        // as it this rolebinding allows him to modify onyxia_owner metadata
        KubernetesClient kubClient = kubernetesClientProvider.getRootClient(region);

        Namespace namespaceToCreate = kubClient.namespaces().createOrReplace(new NamespaceBuilder().withNewMetadata().withName(namespaceId)
                .addToLabels("onyxia_owner", owner.getId()).endMetadata().build());

        RoleBinding bindingToCreate = kubClient.rbac().roleBindings().inNamespace(namespaceId).createOrReplace(new RoleBindingBuilder()
                .withNewMetadata()
                .withLabels(Map.of("createdby","onyxia"))
                .withName("full_control_namespace").withNamespace(namespaceId).endMetadata()
                .withSubjects(new SubjectBuilder().withKind(getSubjectKind(owner)).withName(name)
                        .withApiGroup("rbac.authorization.k8s.io").withNamespace(namespaceId).build())
                .withNewRoleRef().withApiGroup("rbac.authorization.k8s.io").withKind("ClusterRole").withName("admin").endRoleRef().build());

        if (region.getServices().getQuotas().isEnabled()) {
            applyQuotas(region, namespaceId, kubClient);
        }

        return namespaceId;
    }

    private void applyQuotas(Region region, String namespaceId, KubernetesClient kubClient) {
        Quota defaultQuota = region.getServices().getQuotas().getDefaultQuota();
        ResourceQuotaBuilder resourceQuotaBuilder = new ResourceQuotaBuilder();
        resourceQuotaBuilder.withNewMetadata()
                .withLabels(Map.of("createdby","onyxia"))
                .withName("onyxia-quota")
                .withNamespace(namespaceId)
                .endMetadata();

        Map<String, String> quotasToApply = defaultQuota.asMap();

        if (quotasToApply.entrySet().stream().filter(e -> e.getValue() != null).count() == 0) {
            return;
        }

        ResourceQuotaFluent.SpecNested<ResourceQuotaBuilder> resourceQuotaBuilderSpecNested = resourceQuotaBuilder
                .withNewSpec();
        quotasToApply.entrySet().stream().filter(e -> e.getValue() != null).forEach(e -> resourceQuotaBuilderSpecNested.addToHard(e.getKey(),Quantity.parse(e.getValue())));
        resourceQuotaBuilderSpecNested.endSpec();

        ResourceQuota quota = resourceQuotaBuilder.build();
        if (!region.getServices().getQuotas().isAllowUserModification()) {
            kubClient.resourceQuotas().inNamespace(namespaceId).createOrReplace(quota);
        }
        else {
            try {
                kubClient.resourceQuotas().inNamespace(namespaceId).create(quota);
            }
            catch (KubernetesClientException e) {
                if (e.getCode() != 409) {
                    // This is not a "quota already in place" error
                    throw e;
                }
            }
        }
    }

    private String getNameFromOwner(Region region, Owner owner) {
        String username = owner.getId();
        if (owner.getType() == Owner.OwnerType.USER && region.getServices().getUsernamePrefix() != null) {
            username = region.getServices().getUsernamePrefix()+username;
        }
        else if (owner.getType() == Owner.OwnerType.GROUP && region.getServices().getGroupPrefix() != null){
            username = region.getServices().getGroupPrefix()+username;
        }
        return username;
    }

    private String getDefaultNamespace(Region region, Owner owner) {
        if (owner.getType() == Owner.OwnerType.USER) {
            return region.getServices().getNamespacePrefix()+owner.getId();
        }
        else {
            return region.getServices().getGroupNamespacePrefix()+owner.getId();
        }
    }



    public List<Namespace> getNamespaces(Region region, Owner owner) {
        KubernetesClient kubClient = kubernetesClientProvider.getRootClient(region);
        return kubClient.namespaces().withLabel("onyxia_owner",owner.getId()).list().getItems();
    }

    public ResourceQuota getOnyxiaQuota(Region region, Project project, User user) {
        KubernetesClient kubClient = kubernetesClientProvider.getRootClient(region);
        String namespace = determineNamespaceAndCreateIfNeeded(region, project, user);
        return kubClient.resourceQuotas().inNamespace(namespace).withName("onyxia-quota").get();
    }

    private String getSubjectKind(Owner owner) {
        if (owner.getType() == Owner.OwnerType.GROUP) {
            return "Group";
        } else if (owner.getType() == Owner.OwnerType.USER) {
            return "User";
        }
        throw new IllegalArgumentException("Owner type must be one of : USER, GROUP");
    }

    public static class Owner {
        private String id;
        private OwnerType type;

        public static enum OwnerType {
            USER, GROUP;
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