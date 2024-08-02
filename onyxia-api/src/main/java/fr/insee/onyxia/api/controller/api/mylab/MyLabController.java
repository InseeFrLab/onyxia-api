package fr.insee.onyxia.api.controller.api.mylab;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.Catalogs;
import fr.insee.onyxia.api.configuration.NotFoundException;
import fr.insee.onyxia.api.controller.exception.ServiceNotSuspendableException;
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
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "My lab", description = "My services")
@RequestMapping("/my-lab")
@RestController
@SecurityRequirement(name = "auth")
public class MyLabController {
    private final AppsService helmAppsService;

    private final UserProvider userProvider;

    private final CatalogService catalogService;

    private ObjectMapper objectMapper;

    @Autowired
    public MyLabController(
            AppsService helmAppsService,
            UserProvider userProvider,
            CatalogService catalogService,
            ObjectMapper objectMapper) {
        this.helmAppsService = helmAppsService;
        this.userProvider = userProvider;
        this.catalogService = catalogService;
        this.objectMapper = objectMapper;
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

    @PostMapping("/app/rename")
    public void renameApp(
            @Parameter(hidden = true) Region region,
            @Parameter(hidden = true) Project project,
            @RequestBody RenameRequestDTO request)
            throws Exception {
        if (Service.ServiceType.KUBERNETES.equals(region.getServices().getType())) {
            User user = userProvider.getUser(region);
            helmAppsService.rename(
                    region,
                    project,
                    userProvider.getUser(region),
                    request.getServiceID(),
                    request.getFriendlyName());
        }
    }

    @PostMapping("/app/share")
    public void shareApp(
            @Parameter(hidden = true) Region region,
            @Parameter(hidden = true) Project project,
            @RequestBody ShareRequestDTO request)
            throws Exception {
        if (Service.ServiceType.KUBERNETES.equals(region.getServices().getType())) {
            User user = userProvider.getUser(region);
            helmAppsService.share(
                    region,
                    project,
                    userProvider.getUser(region),
                    request.getServiceID(),
                    request.isShare());
        }
    }

    @PostMapping("/app/suspend")
    public void suspendApp(
            @Parameter(hidden = true) Region region,
            @Parameter(hidden = true) Project project,
            @RequestBody SuspendOrResumeRequestDTO request)
            throws Exception {
        suspendOrResume(region, project, request.getServiceID(), true);
    }

    @PostMapping("/app/resume")
    public void resumeApp(
            @Parameter(hidden = true) Region region,
            @Parameter(hidden = true) Project project,
            @RequestBody SuspendOrResumeRequestDTO request)
            throws Exception {
        suspendOrResume(region, project, request.getServiceID(), false);
    }

    private void suspendOrResume(Region region, Project project, String serviceId, boolean suspend)
            throws Exception {
        if (Service.ServiceType.KUBERNETES.equals(region.getServices().getType())) {
            User user = userProvider.getUser(region);
            Service userService =
                    helmAppsService.getUserService(
                            region, project, userProvider.getUser(region), serviceId);
            if (!userService.isSuspendable()) {
                throw new ServiceNotSuspendableException();
            }
            String chart = userService.getChart();
            int split = chart.lastIndexOf('-');
            String chartName = chart.substring(0, split);
            String version = chart.substring(split + 1);
            String catalogId = userService.getCatalogId();
            // This code is for legacy compat for services that were created with Onyxia < v2.7.0
            // before introduction of Onyxia's secret
            if (catalogId == null) {
                List<CatalogWrapper> elligibleCatalogs =
                        catalogService.getCatalogs(region, user).getCatalogs().stream()
                                .filter(
                                        catalog ->
                                                catalog.getCatalog()
                                                        .getPackageByName(chartName)
                                                        .isPresent())
                                .toList();
                if (elligibleCatalogs.isEmpty()) {
                    throw new NotFoundException();
                }
                if (elligibleCatalogs.size() > 1) {
                    throw new IllegalStateException("Chart is present in multiple catalogs, abort");
                }
                CatalogWrapper catalog = elligibleCatalogs.getFirst();
                catalogId = catalog.getId();
            }
            Optional<CatalogWrapper> catalog = catalogService.getCatalogById(catalogId, user);
            if (catalog.isEmpty()) {
                throw new IllegalStateException(
                        "Catalog " + catalogId + " is not available anymore");
            }

            if (suspend) {
                helmAppsService.suspend(
                        region,
                        project,
                        catalog.get().getId(),
                        chartName,
                        version,
                        user,
                        serviceId,
                        catalog.get().getSkipTlsVerify(),
                        catalog.get().getCaFile(),
                        false);
            } else {
                helmAppsService.resume(
                        region,
                        project,
                        catalog.get().getId(),
                        chartName,
                        version,
                        user,
                        serviceId,
                        catalog.get().getSkipTlsVerify(),
                        catalog.get().getCaFile(),
                        false);
            }
        }
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
            summary = "Stream events for the entire user namespace",
            description = "Stream events for the entire user namespace.",
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
    @GetMapping("/events")
    public SseEmitter getEvents(
            @Parameter(hidden = true) Region region, @Parameter(hidden = true) Project project)
            throws Exception {

        if (Service.ServiceType.KUBERNETES.equals(region.getServices().getType())) {
            final SseEmitter emitter = new SseEmitter();
            final CustomWatcher watcher = new CustomWatcher(emitter, objectMapper);
            final Watch watch =
                    helmAppsService.getEvents(
                            region, project, userProvider.getUser(region), watcher);
            emitter.onCompletion(
                    new Runnable() {
                        @Override
                        public void run() {
                            watch.close();
                        }
                    });
            emitter.onError(
                    new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            watch.close();
                        }
                    });
            emitter.onTimeout(
                    new Runnable() {
                        @Override
                        public void run() {
                            watch.close();
                        }
                    });
            return emitter;
        }
        return null;
    }

    public static class CustomWatcher implements Watcher<Event> {

        private final SseEmitter emitter;

        private final ObjectMapper objectMapper;

        public CustomWatcher(SseEmitter emitter, ObjectMapper objectMapper) {
            this.emitter = emitter;
            this.objectMapper = objectMapper;
        }

        @Override
        public void eventReceived(Action action, Event event) {
            try {
                emitter.send(objectMapper.writeValueAsString(event));
            } catch (Exception ignored) {

            }
        }

        @Override
        public void onClose() {
            emitter.complete();
        }

        @Override
        public void onClose(WatcherException e) {
            emitter.complete();
        }
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
        // Substitute userAttribute value with actual value from user's attributes map
        // This is a hack while we wait for this issue to be fixed:
        // https://github.com/InseeFrLab/onyxia-web/issues/410
        if (fusion.containsKey("userAttributes") && fusion.get("userAttributes") != null) {
            Map<String, Object> props = (Map<String, Object>) fusion.get("userAttributes");
            props.replace(
                    "value", user.getAttributes().getOrDefault(props.get("userAttribute"), ""));
        }
        return helmAppsService.installApp(
                region, project, requestDTO, catalogId, pkg, user, fusion, skipTlsVerify, caFile);
    }

    public static class SuspendOrResumeRequestDTO {
        private String serviceID;

        public String getServiceID() {
            return serviceID;
        }

        public void setServiceID(String serviceID) {
            this.serviceID = serviceID;
        }
    }

    public static class RenameRequestDTO {
        private String serviceID;
        private String friendlyName;

        public String getServiceID() {
            return serviceID;
        }

        public void setServiceID(String serviceID) {
            this.serviceID = serviceID;
        }

        public String getFriendlyName() {
            return friendlyName;
        }

        public void setFriendlyName(String friendlyName) {
            this.friendlyName = friendlyName;
        }
    }

    public static class ShareRequestDTO {
        private String serviceID;
        private boolean share;

        public String getServiceID() {
            return serviceID;
        }

        public void setServiceID(String serviceID) {
            this.serviceID = serviceID;
        }

        public boolean isShare() {
            return share;
        }

        public void setShare(boolean share) {
            this.share = share;
        }
    }
}
