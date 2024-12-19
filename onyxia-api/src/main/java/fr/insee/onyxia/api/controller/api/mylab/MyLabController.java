package fr.insee.onyxia.api.controller.api.mylab;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.Catalogs;
import fr.insee.onyxia.api.configuration.NotFoundException;
import fr.insee.onyxia.api.controller.exception.ServiceNotSuspendableException;
import fr.insee.onyxia.api.services.AppsService;
import fr.insee.onyxia.api.services.CatalogService;
import fr.insee.onyxia.api.services.JsonSchemaResolutionService;
import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Pkg;
import fr.insee.onyxia.model.dto.CreateServiceDTO;
import fr.insee.onyxia.model.dto.ServicesListing;
import fr.insee.onyxia.model.helm.Chart;
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
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
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

    private final JsonSchemaResolutionService jsonSchemaResolutionService;

    private ObjectMapper objectMapper;

    @Autowired
    public MyLabController(
            AppsService helmAppsService,
            UserProvider userProvider,
            CatalogService catalogService,
            JsonSchemaResolutionService jsonSchemaResolutionService,
            ObjectMapper objectMapper) {
        this.helmAppsService = helmAppsService;
        this.userProvider = userProvider;
        this.catalogService = catalogService;
        this.jsonSchemaResolutionService = jsonSchemaResolutionService;
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
        futures.add(helmAppsService.getUserServices(region, project, user, groupId));
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
            summary = "Get all versions of a chart from a specific catalog.",
            description =
                    "Get all versions of a chart from a specific catalog, with detailed information on the package including: descriptions, sources, and configuration options.",
            parameters = {
                @Parameter(
                        required = true,
                        name = "catalogId",
                        description = "Unique ID of the enabled catalog for this Onyxia API.",
                        in = ParameterIn.PATH),
                @Parameter(
                        required = true,
                        name = "chartName",
                        description = "Unique name of the chart from the selected catalog.",
                        in = ParameterIn.PATH)
            })
    @GetMapping("/catalogs/{catalogId}/charts/{chartName}")
    public List<Chart> getChartVersions(
            @PathVariable String catalogId, @PathVariable String chartName) {
        List<Chart> charts =
                catalogService.getCharts(catalogId, chartName).orElseThrow(NotFoundException::new);
        return charts;
    }

    @Operation(
            summary = "Get a helm chart from a specific catalog by version.",
            description =
                    "Get a helm chart from a specific catalog by version, with detailed information on the package including: descriptions, sources, and configuration options.",
            parameters = {
                @Parameter(
                        required = true,
                        name = "catalogId",
                        description = "Unique ID of the enabled catalog for this Onyxia API.",
                        in = ParameterIn.PATH),
                @Parameter(
                        required = true,
                        name = "chartName",
                        description = "Unique name of the chart from the selected catalog.",
                        in = ParameterIn.PATH),
                @Parameter(
                        required = true,
                        name = "version",
                        description = "Version of the chart",
                        in = ParameterIn.PATH)
            })
    @GetMapping("schemas/{catalogId}/charts/{chartName}/versions/{version}")
    public JsonNode getSchemas(
            @Parameter(hidden = true) Region region,
            @PathVariable String catalogId,
            @PathVariable String chartName,
            @PathVariable String version) {
        User user = userProvider.getUser(region);
        Chart chart =
                catalogService
                        .getChartByVersion(catalogId, chartName, version)
                        .orElseThrow(NotFoundException::new);

        return jsonSchemaResolutionService.resolveReferences(chart.getConfig(), user.getRoles());
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
        return helmAppsService.getUserService(
                region, project, userProvider.getUser(region), serviceId);
    }

    @PostMapping("/app/rename")
    public void renameApp(
            @Parameter(hidden = true) Region region,
            @Parameter(hidden = true) Project project,
            @RequestBody RenameRequestDTO request)
            throws Exception {
        helmAppsService.rename(
                region,
                project,
                userProvider.getUser(region),
                request.getServiceID(),
                request.getFriendlyName());
    }

    @PostMapping("/app/share")
    public void shareApp(
            @Parameter(hidden = true) Region region,
            @Parameter(hidden = true) Project project,
            @RequestBody ShareRequestDTO request)
            throws Exception {
        helmAppsService.share(
                region,
                project,
                userProvider.getUser(region),
                request.getServiceID(),
                request.isShare());
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
            throw new IllegalStateException("Catalog " + catalogId + " is not available anymore");
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
                    catalog.get().getTimeout(),
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
                    catalog.get().getTimeout(),
                    catalog.get().getCaFile(),
                    false);
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
        return helmAppsService.getLogs(
                region, project, userProvider.getUser(region), serviceId, taskId);
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

        final SseEmitter emitter = new SseEmitter();
        final CustomWatcher watcher = new CustomWatcher(emitter, objectMapper);
        final Watch watch =
                helmAppsService.getEvents(region, project, userProvider.getUser(region), watcher);
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
            try {
                emitter.complete();
            } catch (Exception ignored) {

            }
        }

        @Override
        public void onClose(WatcherException e) {
            try {
                emitter.complete();
            } catch (Exception ignored) {

            }
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
        return helmAppsService.destroyService(
                region, project, userProvider.getUser(region), path, bulk.orElse(false));
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
                        .getPackageByNameAndVersion(
                                requestDTO.getPackageName(), requestDTO.getPackageVersion())
                        .orElseThrow(NotFoundException::new);

        Map<String, Object> fusion = new HashMap<>();
        fusion.putAll((Map<String, Object>) requestDTO.getOptions());

        JSONObject jsonSchema =
                new JSONObject(
                        new JSONTokener(
                                jsonSchemaResolutionService
                                        .resolveReferences(pkg.getConfig(), user.getRoles())
                                        .toString()));

        SchemaLoader loader =
                SchemaLoader.builder()
                        .schemaJson(jsonSchema)
                        .draftV6Support() // or draftV7Support()
                        .build();
        org.everit.json.schema.Schema schema = loader.load().build();
        // Convert the options map to a JSONObject
        JSONObject jsonObject = new JSONObject(fusion);
        // Validate the options object against the schema
        schema.validate(jsonObject);

        boolean skipTlsVerify = catalog.getSkipTlsVerify();
        String caFile = catalog.getCaFile();
        String timeout = catalog.getTimeout();
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
                region,
                project,
                requestDTO,
                catalogId,
                pkg,
                user,
                fusion,
                skipTlsVerify,
                timeout,
                caFile);
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
