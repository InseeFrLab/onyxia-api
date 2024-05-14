package fr.insee.onyxia.api.services.impl;

import static fr.insee.onyxia.api.services.impl.ServiceUrlResolver.getServiceUrls;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.onyxia.api.configuration.kubernetes.HelmClientProvider;
import fr.insee.onyxia.api.configuration.kubernetes.KubernetesClientProvider;
import fr.insee.onyxia.api.controller.exception.NamespaceNotFoundException;
import fr.insee.onyxia.api.events.InstallServiceEvent;
import fr.insee.onyxia.api.events.OnyxiaEventPublisher;
import fr.insee.onyxia.api.events.SuspendResumeServiceEvent;
import fr.insee.onyxia.api.events.UninstallServiceEvent;
import fr.insee.onyxia.api.services.AppsService;
import fr.insee.onyxia.api.services.control.AdmissionControllerHelm;
import fr.insee.onyxia.api.services.control.commons.UrlGenerator;
import fr.insee.onyxia.api.services.control.utils.PublishContext;
import fr.insee.onyxia.api.services.control.xgenerated.XGeneratedContext;
import fr.insee.onyxia.api.services.control.xgenerated.XGeneratedProcessor;
import fr.insee.onyxia.api.services.control.xgenerated.XGeneratedProvider;
import fr.insee.onyxia.api.services.impl.kubernetes.KubernetesService;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Config.Property;
import fr.insee.onyxia.model.catalog.Pkg;
import fr.insee.onyxia.model.dto.CreateServiceDTO;
import fr.insee.onyxia.model.dto.ServicesListing;
import fr.insee.onyxia.model.project.Project;
import fr.insee.onyxia.model.region.Region;
import fr.insee.onyxia.model.service.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.api.model.Secret;
import io.github.inseefrlab.helmwrapper.configuration.HelmConfiguration;
import io.github.inseefrlab.helmwrapper.model.HelmInstaller;
import io.github.inseefrlab.helmwrapper.model.HelmLs;
import io.github.inseefrlab.helmwrapper.model.HelmReleaseInfo;
import io.github.inseefrlab.helmwrapper.service.HelmInstallService;
import io.github.inseefrlab.helmwrapper.service.HelmInstallService.MultipleServiceFound;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;

@org.springframework.stereotype.Service
@Qualifier("Helm")
public class HelmAppsService implements AppsService {

    public static final String SUSPEND_KEY = "global.suspend";

    private static final Logger LOGGER = LoggerFactory.getLogger(HelmAppsService.class);

    private final ObjectMapper mapperHelm;

    private final KubernetesService kubernetesService;

    private final List<AdmissionControllerHelm> admissionControllers;

    private final FastDateFormat helmDateFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
    private final KubernetesClientProvider kubernetesClientProvider;

    private final HelmClientProvider helmClientProvider;

    private final XGeneratedProcessor xGeneratedProcessor;

    private final UrlGenerator urlGenerator;

    final OnyxiaEventPublisher onyxiaEventPublisher;

    @Autowired
    public HelmAppsService(
            @Qualifier("helm") ObjectMapper mapperHelm,
            KubernetesService kubernetesService,
            List<AdmissionControllerHelm> admissionControllers,
            KubernetesClientProvider kubernetesClientProvider,
            HelmClientProvider helmClientProvider,
            XGeneratedProcessor xGeneratedProcessor,
            UrlGenerator urlGenerator,
            OnyxiaEventPublisher onyxiaEventPublisher) {
        this.mapperHelm = mapperHelm;
        this.kubernetesService = kubernetesService;
        this.admissionControllers = admissionControllers;
        this.kubernetesClientProvider = kubernetesClientProvider;
        this.helmClientProvider = helmClientProvider;
        this.xGeneratedProcessor = xGeneratedProcessor;
        this.urlGenerator = urlGenerator;
        this.onyxiaEventPublisher = onyxiaEventPublisher;
    }

    private HelmConfiguration getHelmConfiguration(Region region, User user) {
        return helmClientProvider.getConfiguration(region, user);
    }

    private HelmInstallService getHelmInstallService() {
        return helmClientProvider.defaultHelmInstallService();
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
            final String caFile)
            throws IOException, TimeoutException, InterruptedException {

        PublishContext context = new PublishContext();

        XGeneratedContext xGeneratedContext = xGeneratedProcessor.readContext(pkg);
        XGeneratedProvider xGeneratedProvider =
                new XGeneratedProvider() {
                    @Override
                    public String getGroupId() {
                        return null;
                    }

                    @Override
                    public String getAppId(
                            String scopeName,
                            XGeneratedContext.Scope scope,
                            Property.XGenerated xGenerated) {
                        return pkg.getName();
                    }

                    @Override
                    public String getExternalDns(
                            String scopeName,
                            XGeneratedContext.Scope scope,
                            Property.XGenerated xGenerated) {
                        return urlGenerator.generateUrl(
                                user.getIdep(),
                                pkg.getName(),
                                context.getGlobalContext().getRandomizedId(),
                                scopeName
                                        + (StringUtils.isNotBlank(xGenerated.getName())
                                                ? "-" + xGenerated.getName()
                                                : ""),
                                region.getServices().getExpose().getDomain());
                    }

                    @Override
                    public String getInternalDns(
                            String scopeName,
                            XGeneratedContext.Scope scope,
                            Property.XGenerated xGenerated) {
                        return "";
                    }

                    @Override
                    public String getInitScript(
                            String scopeName,
                            XGeneratedContext.Scope scope,
                            Property.XGenerated xGenerated) {
                        return region.getServices().getInitScript();
                    }
                };
        Map<String, String> xGeneratedValues =
                xGeneratedProcessor.process(xGeneratedContext, xGeneratedProvider);
        xGeneratedProcessor.injectIntoContext(fusion, xGeneratedValues);

        long nbInvalidations =
                admissionControllers.stream()
                        .map(
                                controller ->
                                        controller.validateContract(
                                                region, pkg, fusion, user, context))
                        .filter(b -> !b)
                        .count();
        if (nbInvalidations > 0) {
            throw new AccessDeniedException("Validation failed");
        }
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
                                    caFile);
            InstallServiceEvent installServiceEvent =
                    new InstallServiceEvent(
                            user.getIdep(),
                            namespaceId,
                            requestDTO.getName(),
                            pkg.getName(),
                            catalogId);
            onyxiaEventPublisher.publishEvent(installServiceEvent);
            Map<String, String> metadata = new HashMap<>();
            metadata.put("catalog", Base64.getEncoder().encodeToString(catalogId.getBytes()));
            metadata.put("owner", Base64.getEncoder().encodeToString(user.getIdep().getBytes()));
            if (requestDTO.getFriendlyName()!=null){
                metadata.put("friendlyName", Base64.getEncoder().encodeToString(requestDTO.getFriendlyName().getBytes()));
            }
            byte[] byteArray = new byte[1];
            byteArray[0] = (byte) (requestDTO.isShare() ? 1 : 0);
            metadata.put("share", Base64.getEncoder().encodeToString(byteArray));
            kubernetesService.createOnyxiaSecret(region,namespaceId,requestDTO.getName(),metadata);
            return List.of(res.getManifest());
        } catch (IllegalArgumentException e) {
            throw new AccessDeniedException(e.getMessage());
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
            throw new NamespaceNotFoundException();
        }
        List<HelmLs> installedCharts = null;
        try {
            installedCharts =
                    Arrays.asList(
                            getHelmInstallService()
                                    .listChartInstall(
                                            getHelmConfiguration(region, user),
                                            project.getNamespace()));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(new ServicesListing());
        }
        List<Service> services =
                installedCharts.parallelStream()
                        .map(release -> getHelmApp(region, user, release))
                        .filter(
                                service -> {
                                    boolean canUserSeeThisService = false;
                                    if (project.getGroup() == null) {
                                        // Personal group
                                        canUserSeeThisService = true;
                                    } else {
                                        if (service.getEnv().containsKey("onyxia.share")
                                                && "true"
                                                        .equals(
                                                                service.getEnv()
                                                                        .get("onyxia.share"))) {
                                            // Service has been intentionally shared
                                            canUserSeeThisService = true;
                                        }
                                        if (service.getEnv().containsKey("onyxia.owner")
                                                && user.getIdep()
                                                        .equalsIgnoreCase(
                                                                service.getEnv()
                                                                        .get("onyxia.owner"))) {
                                            // User is owner
                                            canUserSeeThisService = true;
                                        }
                                    }
                                    return canUserSeeThisService;
                                })
                        .collect(Collectors.toList());
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
        HelmReleaseInfo helmReleaseInfo =
                getHelmInstallService()
                        .getAll(
                                getHelmConfiguration(region, user),
                                release.getName(),
                                release.getNamespace());
        Service service =
                getServiceFromRelease(region, release, helmReleaseInfo.getManifest(), user);
        try {
            service.setStartedAt(helmDateFormat.parse(release.getUpdated()).getTime());
        } catch (Exception e) {
            service.setStartedAt(0);
        }
        try {
            KubernetesClient client = kubernetesClientProvider.getUserClient(region, user);
            Secret secret = client.secrets().inNamespace(release.getNamespace()).withName("sh.onyxia.release.v1."+release.getName()).get();
            if (secret!=null && secret.getData() != null){
                if ( secret.getData().containsKey("friendlyName")) {
                   service.setFriendlyName(new String(Base64.getDecoder().decode(secret.getData().get("friendlyName"))));
                }
                if ( secret.getData().containsKey("owner")) {
                   service.setOwner(new String(Base64.getDecoder().decode(secret.getData().get("owner"))));
                }
                if ( secret.getData().containsKey("catalog")) {
                   service.setCatalogId(new String(Base64.getDecoder().decode(secret.getData().get("catalog"))));
                }
                if ( secret.getData().containsKey("share")) {
                    byte[] byteArray = Base64.getDecoder().decode(secret.getData().get("share"));
                    service.setShare(byteArray[0] != 0);     
                }
            }           
        } catch (Exception e) {
            LOGGER.warn("Exception occurred", e);
        }
        service.setId(release.getName());
        service.setName(release.getName());
        service.setSubtitle(release.getChart());
        service.setType(Service.ServiceType.KUBERNETES);
        service.setName(release.getName());
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
                            currentNode ->
                                    mapAppender(result, currentNode, new ArrayList<String>()));
            service.setEnv(result);
            service.setSuspendable(service.getEnv().containsKey(SUSPEND_KEY));
            if (service.getEnv().containsKey(SUSPEND_KEY)) {
                service.setSuspended(Boolean.parseBoolean(service.getEnv().get(SUSPEND_KEY)));
            }
        } catch (Exception e) {
            LOGGER.warn("Exception occurred", e);
        }
        try {
            String notes = helmReleaseInfo.getNotes();
            service.setPostInstallInstructions(notes);
        } catch (Exception e) {
            LOGGER.warn("Exception occurred", e);
        }
        return service;
    }

    @Override
    public void suspend(
            Region region,
            Project project,
            String catalogId,
            Pkg pkg,
            User user,
            String serviceId,
            boolean skipTlsVerify,
            String caFile,
            boolean dryRun)
            throws IOException, InterruptedException, TimeoutException {
        suspendOrResume(
                region,
                project,
                catalogId,
                pkg,
                user,
                serviceId,
                skipTlsVerify,
                caFile,
                dryRun,
                true);
    }

    @Override
    public void resume(
            Region region,
            Project project,
            String catalogId,
            Pkg pkg,
            User user,
            String serviceId,
            boolean skipTlsVerify,
            String caFile,
            boolean dryRun)
            throws IOException, InterruptedException, TimeoutException {
        suspendOrResume(
                region,
                project,
                catalogId,
                pkg,
                user,
                serviceId,
                skipTlsVerify,
                caFile,
                dryRun,
                false);
    }

    public void suspendOrResume(
            Region region,
            Project project,
            String catalogId,
            Pkg pkg,
            User user,
            String serviceId,
            boolean skipTlsVerify,
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
                            catalogId + "/" + pkg.getName(),
                            namespaceId,
                            serviceId,
                            pkg.getVersion(),
                            dryRun,
                            skipTlsVerify,
                            caFile);
        } else {
            getHelmInstallService()
                    .resume(
                            getHelmConfiguration(region, user),
                            catalogId + "/" + pkg.getName(),
                            namespaceId,
                            serviceId,
                            pkg.getVersion(),
                            dryRun,
                            skipTlsVerify,
                            caFile);
        }
        SuspendResumeServiceEvent event =
                new SuspendResumeServiceEvent(
                        user.getIdep(), namespaceId, serviceId, pkg.getName(), catalogId, suspend);
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
                    "Failed to retrieve URLS for release {} namespace {}",
                    release.getName(),
                    release.getNamespace(),
                    e);
            service.setUrls(List.of());
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
                        .collect(Collectors.toList()));

        return service;
    }
}
