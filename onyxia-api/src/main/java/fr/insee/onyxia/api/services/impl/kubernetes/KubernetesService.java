package fr.insee.onyxia.api.services.impl.kubernetes;

import fr.insee.onyxia.api.configuration.kubernetes.KubernetesClientProvider;
import fr.insee.onyxia.api.controller.exception.NamespaceAlreadyExistException;
import fr.insee.onyxia.api.controller.exception.NamespaceNotFoundException;
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
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KubernetesService {

    @Autowired private KubernetesClientProvider kubernetesClientProvider;

    public String createDefaultNamespace(Region region, Owner owner) {
        String namespaceId = getDefaultNamespace(region, owner);
        if (isNamespaceAlreadyExisting(region, namespaceId)) {
            throw new NamespaceAlreadyExistException();
        }
        return createNamespace(region, namespaceId, owner);
    }

    @NotNull
    public String determineNamespaceAndCreateIfNeeded(Region region, Project project, User user) {
        if (region.getServices().isSingleNamespace()) {
            return getCurrentNamespace(region);
        }
        if (StringUtils.isEmpty(project.getNamespace())) {
            throw new NamespaceNotFoundException();
        }
        if (!isNamespaceAlreadyExisting(region, project.getNamespace())) {
            if (!region.getServices().isAllowNamespaceCreation()) {
                throw new NamespaceNotFoundException();
            } else {
                KubernetesService.Owner owner = new KubernetesService.Owner();
                if (project.getGroup() != null) {
                    owner.setId(project.getGroup());
                    owner.setType(Owner.OwnerType.GROUP);
                } else {
                    owner.setId(user.getIdep());
                    owner.setType(KubernetesService.Owner.OwnerType.USER);
                }
                createNamespace(region, project.getNamespace(), owner);
            }
        }
        return project.getNamespace();
    }

    public String getCurrentNamespace(Region region) {
        return kubernetesClientProvider.getRootClient(region).getNamespace();
    }

    private String createNamespace(Region region, String namespaceId, Owner owner) {
        String name = getNameFromOwner(region, owner);

        KubernetesClient kubClient = kubernetesClientProvider.getRootClient(region);

        kubClient
                .namespaces()
                .resource(
                        new NamespaceBuilder()
                                .withNewMetadata()
                                .withName(namespaceId)
                                .addToLabels("onyxia_owner", owner.getId())
                                .endMetadata()
                                .build())
                .create();

        RoleBinding bindingToCreate =
                kubClient
                        .rbac()
                        .roleBindings()
                        .inNamespace(namespaceId)
                        .createOrReplace(
                                new RoleBindingBuilder()
                                        .withNewMetadata()
                                        .withLabels(Map.of("createdby", "onyxia"))
                                        .withName("full_control_namespace")
                                        .withNamespace(namespaceId)
                                        .endMetadata()
                                        .withSubjects(
                                                new SubjectBuilder()
                                                        .withKind(getSubjectKind(owner))
                                                        .withName(name)
                                                        .withApiGroup("rbac.authorization.k8s.io")
                                                        .withNamespace(namespaceId)
                                                        .build())
                                        .withNewRoleRef()
                                        .withApiGroup("rbac.authorization.k8s.io")
                                        .withKind("ClusterRole")
                                        .withName("admin")
                                        .endRoleRef()
                                        .build());

        // Currently, no quotas for groups
        if (owner.getType() == Owner.OwnerType.USER
                && region.getServices().getQuotas().isEnabled()) {
            Quota defaultQuota = region.getServices().getQuotas().getDefaultQuota();
            applyQuotas(
                    namespaceId,
                    kubClient,
                    defaultQuota,
                    !region.getServices().getQuotas().isAllowUserModification());
        }

        return namespaceId;
    }

    private boolean isNamespaceAlreadyExisting(Region region, String namespaceId) {
        return kubernetesClientProvider
                .getRootClient(region)
                .namespaces()
                .list()
                .getItems()
                .stream()
                .anyMatch(ns -> ns.getMetadata().getName().equals(namespaceId));
    }

    private void applyQuotas(
            String namespaceId,
            KubernetesClient kubClient,
            Quota inputQuota,
            boolean overrideExisting) {
        ResourceQuotaBuilder resourceQuotaBuilder = new ResourceQuotaBuilder();
        resourceQuotaBuilder
                .withNewMetadata()
                .withLabels(Map.of("createdby", "onyxia"))
                .withName("onyxia-quota")
                .withNamespace(namespaceId)
                .endMetadata();

        Map<String, String> quotasToApply = inputQuota.asMap();

        if (quotasToApply.entrySet().stream().filter(e -> e.getValue() != null).count() == 0) {
            return;
        }

        ResourceQuotaFluent.SpecNested<ResourceQuotaBuilder> resourceQuotaBuilderSpecNested =
                resourceQuotaBuilder.withNewSpec();
        quotasToApply.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .forEach(
                        e ->
                                resourceQuotaBuilderSpecNested.addToHard(
                                        e.getKey(), Quantity.parse(e.getValue())));
        resourceQuotaBuilderSpecNested.endSpec();

        ResourceQuota quota = resourceQuotaBuilder.build();
        if (overrideExisting) {
            kubClient.resourceQuotas().inNamespace(namespaceId).createOrReplace(quota);
        } else {
            try {
                kubClient.resourceQuotas().inNamespace(namespaceId).create(quota);
            } catch (KubernetesClientException e) {
                if (e.getCode() != 409) {
                    // This is not a "quota already in place" error
                    throw e;
                }
            }
        }
    }

    private String getNameFromOwner(Region region, Owner owner) {
        String username = owner.getId();
        if (owner.getType() == Owner.OwnerType.USER
                && region.getServices().getUsernamePrefix() != null) {
            username = region.getServices().getUsernamePrefix() + username;
        } else if (owner.getType() == Owner.OwnerType.GROUP
                && region.getServices().getGroupPrefix() != null) {
            username = region.getServices().getGroupPrefix() + username;
        }
        return username;
    }

    private String getDefaultNamespace(Region region, Owner owner) {
        if (owner.getType() == Owner.OwnerType.USER) {
            return region.getServices().getNamespacePrefix() + owner.getId();
        } else {
            return region.getServices().getGroupNamespacePrefix() + owner.getId();
        }
    }

    public void applyQuota(Region region, Project project, User user, Quota quota) {
        KubernetesClient kubClient = kubernetesClientProvider.getRootClient(region);
        String namespace = determineNamespaceAndCreateIfNeeded(region, project, user);
        applyQuotas(namespace, kubClient, quota, true);
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
