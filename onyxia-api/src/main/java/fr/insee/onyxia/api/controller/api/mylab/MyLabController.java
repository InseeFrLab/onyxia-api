package fr.insee.onyxia.api.controller.api.mylab;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.Catalogs;
import fr.insee.onyxia.api.configuration.NotFoundException;
import fr.insee.onyxia.api.services.AppsService;
import fr.insee.onyxia.api.services.CatalogService;
import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Pkg;
import fr.insee.onyxia.model.dto.CreateServiceDTO;
import fr.insee.onyxia.model.dto.ServicesListing;
import fr.insee.onyxia.model.project.Project;
import fr.insee.onyxia.model.region.Region;
import fr.insee.onyxia.model.service.Service;
import fr.insee.onyxia.model.service.UninstallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "My lab", description = "My services")
@RequestMapping("/my-lab")
@RestController
@SecurityRequirement(name = "auth")
public class MyLabController {
    private final AppsService helmAppsService;

    private final UserProvider userProvider;

    private final CatalogService catalogService;

    @Autowired
    public MyLabController(
            AppsService helmAppsService, UserProvider userProvider, CatalogService catalogService) {
        this.helmAppsService = helmAppsService;
        this.userProvider = userProvider;
        this.catalogService = catalogService;
    }

    @Operation(
            summary = "List the services installed in a namespace.",
            description =
                    "List the services installed in a namespace. With a Kubernetes backend, utilize Helm to list all installed services in a namespace.",
            parameters = {
                @Parameter(
                        name = "ONYXIA-PROJECT",
                        description =
                                "Project associated with the namespace, defaults to user project.",
                        in = ParameterIn.HEADER,
                        schema =
                                @Schema(
                                        name = "ONYXIA-PROJECT",
                                        type = "string",
                                        description = "Generated project id.",
                                        example = "project-id-example")),
                @Parameter(
                        name = "groupId",
                        description = "Deprectated.",
                        deprecated = true,
                        in = ParameterIn.QUERY)
            })
    @GetMapping("/services")
    public ServicesListing getMyServices(
            @Parameter(hidden = true) Region region,
            @Parameter(hidden = true) Project project,
            @RequestParam(required = false) String groupId)
            throws Exception {
        User user = userProvider.getUser(region);
        ServicesListing dto = new ServicesListing();
        List<CompletableFuture<ServicesListing>> futures = new ArrayList<>();
        if (region.getServices().getType().equals(Service.ServiceType.KUBERNETES)) {
            futures.add(helmAppsService.getUserServices(region, project, user, groupId));
        }
        for (var future : futures) {
            ServicesListing listing = future.get();
            dto.getApps().addAll(listing.getApps());
            dto.getGroups().addAll(listing.getGroups());
        }
        return dto;
    }

    @Operation(
            summary = "List available catalogs and packages for installing for the user.",
            description =
                    "List available catalogs and packages for installing by the user in the first Region configuration of this Onyxia API. This may differ from the catalogs returned from the public endpoint based on the catalog configuration.")
    @GetMapping("/catalogs")
    public Catalogs getMyCatalogs(@Parameter(hidden = true) Region region) {
        User user = userProvider.getUser(region);
        return catalogService.getCatalogs(region, user);
    }

    @Operation(
            summary = "Get the description of an installed service.",
            description =
                    "Get the description of an installed service in the namespace. With Kubernetes backend, an installed service can be seen as a Helm chart. Its unique identifier will be the release name on the namespace.",
            parameters = {
                @Parameter(
                        name = "ONYXIA-PROJECT",
                        description =
                                "Project associated with the namespace, defaults to user project.",
                        in = ParameterIn.HEADER,
                        schema =
                                @Schema(
                                        name = "ONYXIA-PROJECT",
                                        type = "string",
                                        description = "Generated project id.",
                                        example = "project-id-example")),
                @Parameter(
                        name = "serviceId",
                        description = "Unique ID of the installed service in that namespace.",
                        required = true,
                        in = ParameterIn.QUERY)
            })
    @GetMapping("/app")
    public Service getApp(
            @Parameter(hidden = true) Region region,
            @Parameter(hidden = true) Project project,
            @RequestParam("serviceId") String serviceId)
            throws Exception {
        if (Service.ServiceType.KUBERNETES.equals(region.getServices().getType())) {
            return helmAppsService.getUserService(
                    region, project, userProvider.getUser(region), serviceId);
        }
        return null;
    }

    @Operation(
            summary = "Get the logs of a task in an installed service.",
            description =
                    "Get the logs of a task in an installed service. With Kubernetes backend, it can be seen as the logs of a pod in the service.",
            parameters = {
                @Parameter(
                        name = "ONYXIA-PROJECT",
                        description =
                                "Project associated with the namespace, defaults to user project.",
                        in = ParameterIn.HEADER,
                        schema =
                                @Schema(
                                        name = "ONYXIA-PROJECT",
                                        type = "string",
                                        description = "Generated project id.",
                                        example = "project-id-example")),
                @Parameter(
                        name = "serviceId",
                        description = "Unique ID of the installed service in that namespace.",
                        required = true,
                        in = ParameterIn.QUERY),
                @Parameter(
                        name = "taskId",
                        description = "Unique ID of the task from the installed service.",
                        required = true,
                        in = ParameterIn.QUERY)
            })
    @GetMapping("/app/logs")
    public String getLogs(
            @Parameter(hidden = true) Region region,
            @Parameter(hidden = true) Project project,
            @RequestParam("serviceId") String serviceId,
            @RequestParam("taskId") String taskId) {
        if (Service.ServiceType.KUBERNETES.equals(region.getServices().getType())) {
            return helmAppsService.getLogs(
                    region, project, userProvider.getUser(region), serviceId, taskId);
        }
        return null;
    }

    @Operation(
            summary = "Delete an installed service(s) launched through Onyxia.",
            description =
                    "Delete an installed service launched through Onyxia on the namespace given the path, or delete *ALL* installed services on the namespace on bulk deletes. It will prioritize the bulk parameter.",
            parameters = {
                @Parameter(
                        name = "ONYXIA-PROJECT",
                        description =
                                "Project associated with the namespace, defaults to user project.",
                        in = ParameterIn.HEADER,
                        schema =
                                @Schema(
                                        name = "ONYXIA-PROJECT",
                                        type = "string",
                                        description = "Generated project id.",
                                        example = "project-id-example")),
                @Parameter(
                        name = "path",
                        description = "Path to the installed service in that namespace.",
                        in = ParameterIn.QUERY),
                @Parameter(
                        name = "bulk",
                        description =
                                "Wheather to delete all services in a namespace, if set to true, or to look at path.",
                        in = ParameterIn.QUERY)
            })
    @DeleteMapping("/app")
    public UninstallService destroyApp(
            @Parameter(hidden = true) Region region,
            @Parameter(hidden = true) Project project,
            @RequestParam(value = "path", required = false) String path,
            @RequestParam(value = "bulk", required = false) Optional<Boolean> bulk)
            throws Exception {
        if (Service.ServiceType.KUBERNETES.equals(region.getServices().getType())) {
            return helmAppsService.destroyService(
                    region, project, userProvider.getUser(region), path, bulk.orElse(false));
        }
        return null;
    }

    @Operation(
            summary = "Launch a service package through Onyxia.",
            description =
                    "Launch a service package through Onyxia in the namespace, given its catalog, package and configurations out of the available services in this Onyxia instance. More information of available catalogs and packages can be found in the public endpoints.",
            parameters = {
                @Parameter(
                        name = "ONYXIA-PROJECT",
                        description =
                                "Project associated with the namespace, defaults to user project.",
                        in = ParameterIn.HEADER,
                        schema =
                                @Schema(
                                        name = "ONYXIA-PROJECT",
                                        type = "string",
                                        description = "Generated project id.",
                                        example = "project-id-example"))
            })
    @PutMapping("/app")
    public Object publishService(
            @Parameter(hidden = true) Region region,
            @Parameter(hidden = true) Project project,
            @RequestBody CreateServiceDTO requestDTO)
            throws Exception {
        return publishApps(region, project, requestDTO);
    }

    private Collection<Object> publishApps(
            Region region, Project project, CreateServiceDTO requestDTO) throws Exception {
        String catalogId = "internal";
        if (requestDTO.getCatalogId() != null && !requestDTO.getCatalogId().isEmpty()) {
            catalogId = requestDTO.getCatalogId();
        }
        User user = userProvider.getUser(region);
        CatalogWrapper catalog =
                catalogService.getCatalogById(catalogId, user).orElseThrow(NotFoundException::new);

        Pkg pkg =
                catalog.getCatalog()
                        .getPackageByName(requestDTO.getPackageName())
                        .orElseThrow(NotFoundException::new);

        boolean skipTlsVerify = catalog.getSkipTlsVerify();
        String caFile = catalog.getCaFile();
        Map<String, Object> fusion = new HashMap<>();
        fusion.putAll((Map<String, Object>) requestDTO.getOptions());
        return helmAppsService.installApp(
                region, project, requestDTO, catalogId, pkg, user, fusion, skipTlsVerify, caFile);
    }
}
