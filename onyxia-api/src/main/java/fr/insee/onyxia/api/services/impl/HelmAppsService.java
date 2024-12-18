package fr.insee.onyxia.api.services.impl;

import static fr.insee.onyxia.api.services.impl.HelmReleaseHealthResolver.checkHelmReleaseHealth;
import static fr.insee.onyxia.api.services.impl.ServiceUrlResolver.getServiceUrls;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.onyxia.api.configuration.kubernetes.HelmClientProvider;
import fr.insee.onyxia.api.configuration.kubernetes.KubernetesClientProvider;
import fr.insee.onyxia.api.controller.RestExceptionTypes;
import fr.insee.onyxia.api.controller.exception.*;
import fr.insee.onyxia.api.controller.exception.CustomKubernetesException;
import fr.insee.onyxia.api.events.InstallServiceEvent;
import fr.insee.onyxia.api.events.OnyxiaEventPublisher;
import fr.insee.onyxia.api.events.SuspendResumeServiceEvent;
import fr.insee.onyxia.api.events.UninstallServiceEvent;
import fr.insee.onyxia.api.services.AppsService;
import fr.insee.onyxia.api.services.impl.kubernetes.KubernetesService;
import fr.insee.onyxia.api.services.utils.Base64Utils;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Pkg;
import fr.insee.onyxia.model.dto.CreateServiceDTO;
import fr.insee.onyxia.model.dto.ServicesListing;
import fr.insee.onyxia.model.project.Project;
import fr.insee.onyxia.model.region.Region;
import fr.insee.onyxia.model.service.*;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.github.inseefrlab.helmwrapper.configuration.HelmConfiguration;
import io.github.inseefrlab.helmwrapper.model.HelmInstaller;
import io.github.inseefrlab.helmwrapper.model.HelmLs;
import io.github.inseefrlab.helmwrapper.model.HelmReleaseInfo;
import io.github.inseefrlab.helmwrapper.service.HelmInstallService;
import io.github.inseefrlab.helmwrapper.service.HelmInstallService.MultipleServiceFound;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

@org.springframework.stereotype.Service
@Qualifier("Helm")
public class HelmAppsService implements AppsService {

    public static final String SUSPEND_KEY = "global.suspend";

    private static final Logger LOGGER = LoggerFactory.getLogger(HelmAppsService.class);

    private final ObjectMapper mapperHelm;

    private final KubernetesService kubernetesService;

    private final FastDateFormat helmDateFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
    private final KubernetesClientProvider kubernetesClientProvider;

    private final HelmClientProvider helmClientProvider;

    final OnyxiaEventPublisher onyxiaEventPublisher;

    public static final String ONYXIA_SECRET_PREFIX = "sh.onyxia.release.v1.";
    private static final String CATALOG = "catalog";
    private static final String OWNER = "owner";
    private static final String FRIENDLY_NAME = "friendlyName";
    private static final String SHARE = "share";

    @Autowired
    public HelmAppsService(
            @Qualifier("helm") ObjectMapper mapperHelm,
            KubernetesService kubernetesService,
            KubernetesClientProvider kubernetesClientProvider,
            HelmClientProvider helmClientProvider,
            OnyxiaEventPublisher onyxiaEventPublisher) {
        this.mapperHelm = mapperHelm;
        this.kubernetesService = kubernetesService;
        this.kubernetesClientProvider = kubernetesClientProvider;
        this.helmClientProvider = helmClientProvider;
        this.onyxiaEventPublisher = onyxiaEventPublisher;
    }

    private HelmConfiguration getHelmConfiguration(Region region, User user) {
        return helmClientProvider.getConfiguration(region, user);
    }

    private HelmInstallService getHelmInstallService() {
        return helmClientProvider.defaultHelmInstallService();
    }

    private ProblemDetail createProblemDetail(
            HttpStatus status, URI type, String title, String detail, String instance) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setType(type);
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        problemDetail.setInstance(URI.create(instance));
        return problemDetail;
    }

    @Override
    public Collection<Object> installApp(
            Region region,
            Project project,
            CreateServiceDTO requestDTO,
            String catalogId,
            Pkg pkg,
            User user,
            Map<String, Object> fusion,
            final boolean skipTlsVerify,
            String timeout,
            final String caFile)
            throws IOException, TimeoutException, InterruptedException {

        File values = File.createTempFile("values", ".yaml");
        mapperHelm.writeValue(values, fusion);
        String namespaceId =
                kubernetesService.determineNamespaceAndCreateIfNeeded(region, project, user);
        try {
            HelmInstaller res =
                    getHelmInstallService()
                            .installChart(
                                    getHelmConfiguration(region, user),
                                    catalogId + "/" + pkg.getName(),
                                    namespaceId,
                                    requestDTO.getName(),
                                    requestDTO.getPackageVersion(),
                                    requestDTO.isDryRun(),
                                    values,
                                    null,
                                    skipTlsVerify,
                                    timeout,
                                    caFile);
            InstallServiceEvent installServiceEvent =
                    new InstallServiceEvent(
                            user.getIdep(),
                            namespaceId,
                            requestDTO.getName(),
                            pkg.getName(),
                            catalogId,
                            requestDTO.getFriendlyName());
            onyxiaEventPublisher.publishEvent(installServiceEvent);
            Map<String, String> metadata = new HashMap<>();
            metadata.put(CATALOG, Base64Utils.base64Encode(catalogId));
            metadata.put(OWNER, Base64Utils.base64Encode(user.getIdep()));
            if (requestDTO.getFriendlyName() != null) {
                metadata.put(FRIENDLY_NAME, Base64Utils.base64Encode(requestDTO.getFriendlyName()));
            }
            metadata.put(SHARE, Base64Utils.base64Encode(String.valueOf(requestDTO.isShare())));
            kubernetesService.createOnyxiaSecret(
                    region, namespaceId, requestDTO.getName(), metadata);
            return List.of(res.getManifest());
        } catch (IllegalArgumentException e) {
            String instanceUri =
                    String.format(
                            "/install-app/%s/%s/%s", namespaceId, catalogId, requestDTO.getName());
            throw new CustomKubernetesException(
                    createProblemDetail(
                            HttpStatus.BAD_REQUEST,
                            RestExceptionTypes.INVALID_ARGUMENT,
                            "Invalid Argument",
                            e.getMessage(),
                            instanceUri));
        } catch (Exception e) {
            LOGGER.error("Unexpected error during app installation", e);

            String instanceUri =
                    String.format(
                            "/install-app/%s/%s/%s", namespaceId, catalogId, requestDTO.getName());

            throw new CustomKubernetesException(
                    createProblemDetail(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            RestExceptionTypes.INSTALLATION_FAILURE,
                            "Installation Failure",
                            "An unexpected error occurred while installing the app. Please try again later.",
                            instanceUri));
        } finally {
            if (!values.delete()) {
                LOGGER.warn("Failed to delete values file, path {}", values.getAbsolutePath());
            }
        }
    }

    @Override
    public CompletableFuture<ServicesListing> getUserServices(
            Region region, Project project, User user) throws IOException, IllegalAccessException {
        return getUserServices(region, project, user, null);
    }

    @Override
    public CompletableFuture<ServicesListing> getUserServices(
            Region region, Project project, User user, String groupId)
            throws IOException, IllegalAccessException {
        if (groupId != null) {
            LOGGER.debug("STUB : group listing is currently not supported on helm");
            return CompletableFuture.completedFuture(new ServicesListing());
        }
        if (StringUtils.isEmpty(project.getNamespace())) {
            String instanceUri = "/projects/" + project.getId() + "/namespace";
            throw new CustomKubernetesException(
                    createProblemDetail(
                            HttpStatus.NOT_FOUND,
                            RestExceptionTypes.NAMESPACE_NOT_FOUND,
                            "Namespace Not Found",
                            "The namespace for the provided project is empty or not defined.",
                            instanceUri));
        }
        List<HelmLs> installedCharts;
        try {
            installedCharts =
                    Arrays.asList(
                            getHelmInstallService()
                                    .listChartInstall(
                                            getHelmConfiguration(region, user),
                                            project.getNamespace()));
        } catch (Exception e) {
            LOGGER.error(
                    "Failed to list installed Helm charts for namespace {}",
                    project.getNamespace(),
                    e);
            String instanceUri = "/namespaces/" + project.getNamespace() + "/helm-list";
            throw new CustomKubernetesException(
                    createProblemDetail(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            RestExceptionTypes.HELM_LIST_FAILURE,
                            "Helm List Failure",
                            "Failed to retrieve the list of installed Helm charts. Please try again later.",
                            instanceUri));
        }
        List<Service> services =
                installedCharts.parallelStream()
                        .map(release -> getHelmApp(region, user, release))
                        .filter(
                                service ->
                                        // Check if user can see this service
                                        project.getGroup() == null
                                                || service.isShare()
                                                || user.getIdep()
                                                        .equalsIgnoreCase(service.getOwner()))
                        .toList();
        ServicesListing listing = new ServicesListing();
        listing.setApps(services);
        return CompletableFuture.completedFuture(listing);
    }

    @Override
    public String getLogs(
            Region region, Project project, User user, String serviceId, String taskId) {
        KubernetesClient client = kubernetesClientProvider.getUserClient(region, user);
        return client.pods()
                .inNamespace(
                        kubernetesService.determineNamespaceAndCreateIfNeeded(
                                region, project, user))
                .withName(taskId)
                .getLog();
    }

    @Override
    public Watch getEvents(
            Region region,
            Project project,
            User user,
            Watcher<io.fabric8.kubernetes.api.model.Event> watcher) {
        KubernetesClient client = kubernetesClientProvider.getUserClient(region, user);
        return client.v1()
                .events()
                .inNamespace(
                        kubernetesService.determineNamespaceAndCreateIfNeeded(
                                region, project, user))
                .watch(watcher);
    }

    @Override
    public Service getUserService(Region region, Project project, User user, String serviceId)
            throws MultipleServiceFound, ParseException {
        if (serviceId.startsWith("/")) {
            serviceId = serviceId.substring(1);
        }
        HelmLs result =
                getHelmInstallService()
                        .getAppById(
                                getHelmConfiguration(region, user),
                                serviceId,
                                kubernetesService.determineNamespaceAndCreateIfNeeded(
                                        region, project, user));
        return getHelmApp(region, user, result);
    }

    @Override
    public UninstallService destroyService(
            Region region, Project project, User user, final String path, boolean bulk)
            throws Exception {
        final String namespace =
                kubernetesService.determineNamespaceAndCreateIfNeeded(region, project, user);
        UninstallService result = new UninstallService();
        result.setPath(path);
        int status = 0;
        if (bulk) {
            // If bulk in kub we ignore the path and delete every helm release
            HelmLs[] releases =
                    getHelmInstallService()
                            .listChartInstall(getHelmConfiguration(region, user), namespace);
            for (HelmLs release : releases) {
                status =
                        Math.max(
                                0,
                                getHelmInstallService()
                                        .uninstaller(
                                                getHelmConfiguration(region, user),
                                                release.getName(),
                                                namespace));
                UninstallServiceEvent uninstallServiceEvent =
                        new UninstallServiceEvent(namespace, release.getName(), user.getIdep());
                onyxiaEventPublisher.publishEvent(uninstallServiceEvent);
            }
        } else {
            // Strip / if present
            String cannonicalPath = path.startsWith("/") ? path.substring(1) : path;
            status =
                    getHelmInstallService()
                            .uninstaller(
                                    getHelmConfiguration(region, user), cannonicalPath, namespace);
            UninstallServiceEvent uninstallServiceEvent =
                    new UninstallServiceEvent(namespace, cannonicalPath, user.getIdep());
            onyxiaEventPublisher.publishEvent(uninstallServiceEvent);
        }
        result.setSuccess(status == 0);
        return result;
    }

    private Service getHelmApp(Region region, User user, HelmLs release) {
        HelmReleaseInfo helmReleaseInfo;
        try {
            helmReleaseInfo =
                    getHelmInstallService()
                            .getAll(
                                    getHelmConfiguration(region, user),
                                    release.getName(),
                                    release.getNamespace());
        } catch (Exception e) {
            LOGGER.error(
                    "Failed to retrieve Helm release info for release {} in namespace {}",
                    release.getName(),
                    release.getNamespace(),
                    e);
            String instanceUri = "/releases/" + release.getName();
            throw new CustomKubernetesException(
                    createProblemDetail(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            RestExceptionTypes.HELM_RELEASE_FETCH_FAILURE,
                            "Helm Release Fetch Failure",
                            "Failed to retrieve Helm release information.",
                            instanceUri));
        }

        Service service =
                getServiceFromRelease(region, release, helmReleaseInfo.getManifest(), user);

        try {
            service.setStartedAt(helmDateFormat.parse(release.getUpdated()).getTime());
        } catch (Exception e) {
            LOGGER.warn("Failed to parse release updated date for {}", release.getName(), e);
            service.setStartedAt(0); // Fallback to 0 if parsing fails
        }
        try {
            KubernetesClient client = kubernetesClientProvider.getUserClient(region, user);
            Secret secret =
                    client.secrets()
                            .inNamespace(release.getNamespace())
                            .withName(ONYXIA_SECRET_PREFIX + release.getName())
                            .get();
            if (secret != null && secret.getData() != null) {
                Map<String, String> data = secret.getData();
                if (data.containsKey(FRIENDLY_NAME)) {
                    service.setFriendlyName(Base64Utils.base64Decode(data.get(FRIENDLY_NAME)));
                }
                if (data.containsKey(OWNER)) {
                    service.setOwner(Base64Utils.base64Decode(data.get(OWNER)));
                }
                if (data.containsKey(CATALOG)) {
                    service.setCatalogId(Base64Utils.base64Decode(data.get(CATALOG)));
                }
                if (data.containsKey(SHARE)) {
                    service.setShare(
                            Boolean.parseBoolean(Base64Utils.base64Decode(data.get(SHARE))));
                }
            }
        } catch (Exception e) {
            LOGGER.warn(
                    "Failed to retrieve or decode Onyxia secret for release {}",
                    release.getName(),
                    e);
        }

        service.setId(release.getName());
        service.setName(release.getName());
        service.setSubtitle(release.getChart());
        service.setNamespace(release.getNamespace());
        service.setRevision(release.getRevision());
        service.setStatus(release.getStatus());
        service.setUpdated(release.getUpdated());
        service.setChart(release.getChart());
        service.setAppVersion(release.getAppVersion());

        try {
            String values = helmReleaseInfo.getUserSuppliedValues();
            JsonNode node = mapperHelm.readTree(values);
            Map<String, String> result = new HashMap<>();
            node.fields()
                    .forEachRemaining(
                            currentNode -> mapAppender(result, currentNode, new ArrayList<>()));
            service.setEnv(result);
            service.setSuspendable(service.getEnv().containsKey(SUSPEND_KEY));
            if (service.getEnv().containsKey(SUSPEND_KEY)) {
                service.setSuspended(Boolean.parseBoolean(service.getEnv().get(SUSPEND_KEY)));
            }
        } catch (Exception e) {
            LOGGER.warn(
                    "Failed to parse user-supplied values for release {}", release.getName(), e);
        }

        try {
            String notes = helmReleaseInfo.getNotes();
            service.setPostInstallInstructions(notes);
        } catch (Exception e) {
            LOGGER.warn(
                    "Failed to retrieve post-install instructions for release {}",
                    release.getName(),
                    e);
        }

        return service;
    }

    @Override
    public void rename(
            Region region, Project project, User user, String serviceId, String friendlyName)
            throws IOException, InterruptedException, TimeoutException {
        patchOnyxiaSecret(region, project, user, serviceId, Map.of(FRIENDLY_NAME, friendlyName));
    }

    @Override
    public void share(Region region, Project project, User user, String serviceId, boolean share)
            throws IOException, InterruptedException, TimeoutException {
        patchOnyxiaSecret(region, project, user, serviceId, Map.of(SHARE, String.valueOf(share)));
    }

    private void patchOnyxiaSecret(
            Region region, Project project, User user, String serviceId, Map<String, String> data) {
        String namespaceId =
                kubernetesService.determineNamespaceAndCreateIfNeeded(region, project, user);
        KubernetesClient client = kubernetesClientProvider.getUserClient(region, user);
        Secret secret =
                client.secrets()
                        .inNamespace(namespaceId)
                        .withName(ONYXIA_SECRET_PREFIX + serviceId)
                        .get();
        if (secret != null) {
            Map<String, String> secretData =
                    secret.getData() != null ? secret.getData() : new HashMap<>();
            data.forEach((k, v) -> secretData.put(k, Base64Utils.base64Encode(v)));
            secret.setData(secretData);
            if (secret.getMetadata().getManagedFields() != null) {
                secret.getMetadata().getManagedFields().clear();
            }
            client.secrets()
                    .inNamespace(namespaceId)
                    .resource(secret)
                    .forceConflicts()
                    .serverSideApply();
        } else {
            Map<String, String> metadata = new HashMap<>();
            metadata.put(OWNER, user.getIdep());
            metadata.putAll(data);
            kubernetesService.createOnyxiaSecret(region, namespaceId, serviceId, metadata);
        }
    }

    @Override
    public void suspend(
            Region region,
            Project project,
            String catalogId,
            String chartName,
            String version,
            User user,
            String serviceId,
            boolean skipTlsVerify,
            String timeout,
            String caFile,
            boolean dryRun)
            throws IOException, InterruptedException, TimeoutException {
        suspendOrResume(
                region,
                project,
                catalogId,
                chartName,
                version,
                user,
                serviceId,
                skipTlsVerify,
                timeout,
                caFile,
                dryRun,
                true);
    }

    @Override
    public void resume(
            Region region,
            Project project,
            String catalogId,
            String chartName,
            String version,
            User user,
            String serviceId,
            boolean skipTlsVerify,
            String timeout,
            String caFile,
            boolean dryRun)
            throws IOException, InterruptedException, TimeoutException {
        suspendOrResume(
                region,
                project,
                catalogId,
                chartName,
                version,
                user,
                serviceId,
                skipTlsVerify,
                timeout,
                caFile,
                dryRun,
                false);
    }

    public void suspendOrResume(
            Region region,
            Project project,
            String catalogId,
            String chartName,
            String version,
            User user,
            String serviceId,
            boolean skipTlsVerify,
            String timeout,
            String caFile,
            boolean dryRun,
            boolean suspend)
            throws IOException, InterruptedException, TimeoutException {
        String namespaceId =
                kubernetesService.determineNamespaceAndCreateIfNeeded(region, project, user);
        if (suspend) {
            getHelmInstallService()
                    .suspend(
                            getHelmConfiguration(region, user),
                            catalogId + "/" + chartName,
                            namespaceId,
                            serviceId,
                            version,
                            dryRun,
                            skipTlsVerify,
                            timeout,
                            caFile);
        } else {
            getHelmInstallService()
                    .resume(
                            getHelmConfiguration(region, user),
                            catalogId + "/" + chartName,
                            namespaceId,
                            serviceId,
                            version,
                            dryRun,
                            skipTlsVerify,
                            timeout,
                            caFile);
        }
        SuspendResumeServiceEvent event =
                new SuspendResumeServiceEvent(
                        user.getIdep(), namespaceId, serviceId, chartName, catalogId, suspend);
        onyxiaEventPublisher.publishEvent(event);
    }

    private void mapAppender(
            Map<String, String> result, Map.Entry<String, JsonNode> node, List<String> names) {
        names.add(node.getKey());
        if (node.getValue().isValueNode()) {
            String name = String.join(".", names);
            result.put(name, node.getValue().asText());
        } else {
            node.getValue()
                    .fields()
                    .forEachRemaining(
                            nested -> mapAppender(result, nested, new ArrayList<>(names)));
        }
    }

    private Service getServiceFromRelease(
            Region region, HelmLs release, String manifest, User user) {
        KubernetesClient client = kubernetesClientProvider.getUserClient(region, user);

        Service service = new Service();

        try {
            List<String> urls = getServiceUrls(region, manifest, client);
            service.setUrls(urls);
        } catch (Exception e) {
            LOGGER.warn(
                    "Failed to retrieve URLs for release {} in namespace {}. Region: {}, User: {}",
                    release.getName(),
                    release.getNamespace(),
                    region.getName(),
                    user.getIdep(),
                    e);
            service.setUrls(List.of());
        }

        try {
            List<HealthCheckResult> controllers =
                    checkHelmReleaseHealth(release.getNamespace(), manifest, client);
            service.setControllers(controllers);
        } catch (Exception e) {
            LOGGER.warn(
                    "Failed to retrieve controllers for release {} in namespace {}. Region: {}, User: {}",
                    release.getName(),
                    release.getNamespace(),
                    region.getName(),
                    user.getIdep(),
                    e);
            service.setControllers(List.of());
        }

        service.setInstances(1);

        service.setTasks(
                client
                        .pods()
                        .inNamespace(release.getNamespace())
                        .withLabel("app.kubernetes.io/instance", release.getName())
                        .list()
                        .getItems()
                        .stream()
                        .map(
                                pod -> {
                                    Task currentTask = new Task();
                                    currentTask.setId(pod.getMetadata().getName());
                                    TaskStatus status = new TaskStatus();
                                    status.setStatus(pod.getStatus().getPhase());
                                    status.setReason(
                                            pod.getStatus().getContainerStatuses().stream()
                                                    .filter(
                                                            cstatus ->
                                                                    cstatus.getState().getWaiting()
                                                                            != null)
                                                    .map(
                                                            cstatus ->
                                                                    cstatus.getState()
                                                                            .getWaiting()
                                                                            .getReason())
                                                    .findFirst()
                                                    .orElse(null));
                                    pod.getStatus()
                                            .getContainerStatuses()
                                            .forEach(
                                                    c -> {
                                                        Container container = new Container();
                                                        container.setName(c.getName());
                                                        container.setReady(c.getReady());
                                                        currentTask.getContainers().add(container);
                                                    });
                                    currentTask.setStatus(status);
                                    return currentTask;
                                })
                        .toList());

        return service;
    }
}
