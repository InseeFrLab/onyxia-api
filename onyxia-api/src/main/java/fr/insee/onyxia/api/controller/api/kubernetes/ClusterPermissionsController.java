package fr.insee.onyxia.api.controller.api.kubernetes;

import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.impl.kubernetes.KubernetesService;
import fr.insee.onyxia.model.region.Region;
import io.fabric8.kubernetes.api.model.Namespace;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Kubernetes permissions", description = "Manage user access to Kubernetes")
@RequestMapping("/kubernetes/permissions")
@RestController
@SecurityRequirement(name = "auth")
@ConditionalOnProperty(name = "kubernetes.enabled", havingValue = "true")
public class ClusterPermissionsController {

    @Autowired
    private KubernetesService kubernetesService;

    @Autowired
    private UserProvider userProvider;

    @GetMapping
    public List<String> getNamespaces(@Parameter(hidden = true) Region region) {
        List<String> userNamespaces;
        userNamespaces = kubernetesService.getNamespaces(region, getOwnerFromUser()).stream().map(namespace -> namespace.getMetadata().getName()).collect(Collectors.toList());
        return userNamespaces;
    }

    @PostMapping
    public void createNamespace(@Parameter(hidden = true) Region region, @RequestBody CreateNamespaceRequest request) {
        kubernetesService.createNamespace(region, request.getNamespaceName(), getOwnerFromUser());
    }


    private KubernetesService.Owner getOwnerFromUser() {
        KubernetesService.Owner owner = new KubernetesService.Owner();
        owner.setId(userProvider.getUser().getIdep());
        owner.setType(KubernetesService.Owner.OwnerType.USER);
        return owner;
    }

    public static class CreateNamespaceRequest {

        private String namespaceName;

        public String getNamespaceName() {
            return namespaceName;
        }

        public void setNamespaceName(String namespaceName) {
            this.namespaceName = namespaceName;
        }
    }
}
