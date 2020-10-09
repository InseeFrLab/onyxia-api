package fr.insee.onyxia.api.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.onyxia.api.configuration.kubernetes.HelmClientProvider;
import fr.insee.onyxia.api.configuration.kubernetes.KubernetesClientProvider;
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
import fr.insee.onyxia.model.region.Region;
import fr.insee.onyxia.model.service.*;
import io.fabric8.kubernetes.api.model.EventList;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.github.inseefrlab.helmwrapper.model.HelmInstaller;
import io.github.inseefrlab.helmwrapper.model.HelmLs;
import io.github.inseefrlab.helmwrapper.service.HelmInstallService;
import io.github.inseefrlab.helmwrapper.service.HelmInstallService.MultipleServiceFound;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@org.springframework.stereotype.Service
@Qualifier("Helm")
public class HelmAppsService implements AppsService {

    @Autowired
    private KubernetesService kubernetesService;

    @Autowired
    @Qualifier("helm")
    ObjectMapper mapperHelm;

    @Autowired(required = false)
    private List<AdmissionControllerHelm> admissionControllers = new ArrayList<>();

    private SimpleDateFormat helmDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final Logger LOGGER = LoggerFactory.getLogger(HelmAppsService.class);

    @Autowired
    private KubernetesClientProvider kubernetesClientProvider;

    @Autowired
    private HelmClientProvider helmClientProvider;

    @Autowired
    private XGeneratedProcessor xGeneratedProcessor;

    @Autowired
    private UrlGenerator urlGenerator;

    private HelmInstallService getHelmInstallService(Region region) {
        return helmClientProvider.getHelmInstallServiceForRegion(region);
    }

    @Override
    public Collection<Object> installApp(Region region,CreateServiceDTO requestDTO, String catalogId, Pkg pkg,
            User user, Map<String, Object> fusion) throws IOException, TimeoutException, InterruptedException {
        Region.CloudshellConfiguration cloudshellConfiguration = region.getServices().getCloudshell();
        boolean isCloudshell =false;
        if (cloudshellConfiguration != null && catalogId.equals(cloudshellConfiguration.getCatalogId()) && pkg.getName().equals(cloudshellConfiguration.getPackageName())) {
            isCloudshell =  true;
        }

        PublishContext context = new PublishContext();

        XGeneratedContext xGeneratedContext = xGeneratedProcessor.readContext(pkg);
        XGeneratedProvider xGeneratedProvider = new XGeneratedProvider() {
            @Override
            public String getGroupId() {
                return null;
            }

            @Override
            public String getAppId(String scopeName, XGeneratedContext.Scope scope, Property.XGenerated xGenerated) {
                return pkg.getName();
            }

            @Override
            public String getExternalDns(String scopeName, XGeneratedContext.Scope scope, Property.XGenerated xGenerated) {
                return urlGenerator.generateUrl(user.getIdep(), pkg.getName(),
                        context.getGlobalContext().getRandomizedId(), "", region.getServices().getExpose().getDomain());
            }

            @Override
            public String getInternalDns(String scopeName, XGeneratedContext.Scope scope, Property.XGenerated xGenerated) {
                return "";
            }

            @Override
            public String getNetworkName(String scopeName, XGeneratedContext.Scope scope, Property.XGenerated xGenerated) {
                return region.getServices().getNetwork();
            }

            @Override
            public String getInitScript(String scopeName, XGeneratedContext.Scope scope, Property.XGenerated xGenerated) {
                return region.getServices().getInitScript();
            }
        };
        Map<String,String> xGeneratedValues = xGeneratedProcessor.process(xGeneratedContext,xGeneratedProvider);
        xGeneratedProcessor.injectIntoContext(fusion,xGeneratedValues);

        long nbInvalidations = admissionControllers.stream().map(controller -> controller.validateContract(region, pkg, fusion, user, context))
                .filter(b -> !b).count();
        if (nbInvalidations > 0) {
            throw new AccessDeniedException("Validation failed");
        }
        File values = File.createTempFile("values", ".yaml");
        mapperHelm.writeValue(values, fusion);
        String namespaceId = determineNamespace(region, region.getServices().getNamespacePrefix(), user);
        String name = isCloudshell ? "cloudshell" : null;
        HelmInstaller res = getHelmInstallService(region).installChart(catalogId + "/" + pkg.getName(), namespaceId, name, requestDTO.isDryRun(),
                values);
        values.delete();
        return List.of(res.getManifest());
    }

    @Override
    public CompletableFuture<ServicesListing> getUserServices(Region region,User user) throws IOException, IllegalAccessException {
        return getUserServices(region, user, null);
    }

    @Override
    public CompletableFuture<ServicesListing> getUserServices(Region region,User user, String groupId)
            throws IOException, IllegalAccessException {
        if (groupId != null) {
            LOGGER.debug("STUB : group listing is currently not supported on helm");
            return CompletableFuture.completedFuture(new ServicesListing());
        }
        List<HelmLs> installedCharts = null;
        try {
            installedCharts = Arrays.asList(getHelmInstallService(region).listChartInstall(region.getServices().getNamespacePrefix() + user.getIdep()));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(new ServicesListing());
        }
        List<Service> services = new ArrayList<>();
        for (HelmLs release : installedCharts) {
            services.add(getHelmApp(region,release));
        }
        ServicesListing listing = new ServicesListing();
        listing.setApps(services);
        return CompletableFuture.completedFuture(listing);
    }

    @Override
    public String getLogs(Region region,User user, String serviceId, String taskId) {
        KubernetesClient client = kubernetesClientProvider.getClientForRegion(region);
        return client.pods().inNamespace(determineNamespace(region, region.getServices().getNamespacePrefix(),user)).withName(taskId).getLog();
    }

    @Override
    public Service getUserService(Region region, User user, String serviceId) throws MultipleServiceFound, ParseException {
        if (serviceId.startsWith("/")) {
            serviceId = serviceId.substring(1);
        }
        HelmLs result = getHelmInstallService(region).getAppById(serviceId, determineNamespace(region, region.getServices().getNamespacePrefix(),user));
        return getHelmApp(region,result);
    }

    @Override
    public UninstallService destroyService(Region region, User user, final String path, boolean bulk) throws Exception {
        final String namespace = determineNamespace(region, region.getServices().getNamespacePrefix(), user);
        UninstallService result = new UninstallService();
        result.setPath(path);
        HelmInstallService helmService = getHelmInstallService(region);
        int status = 0;
        if (bulk) {
            // If bulk in kub we ignore the path and delete every helm release
            HelmLs[] releases = getHelmInstallService(region).listChartInstall(namespace);
            for (int i = 0; i <releases.length; i++){
                status = Math.max(0,helmService.uninstaller(releases[i].getName(),namespace));
            }
        }
        else {
            // Strip / if present
            String cannonicalPath = path.startsWith("/") ? path.substring(1) : path;
            status = getHelmInstallService(region).uninstaller(cannonicalPath, namespace);
        }

        result.setSuccess(status == 0);
        return result;
    }

    private Service getHelmApp(Region region, HelmLs release) {
        String manifest = getHelmInstallService(region).getManifest(release.getName(), release.getNamespace());
        Service service = getServiceFromRelease(region, release, manifest);
        service.setStatus(findAppStatus(release));
        try {
            service.setStartedAt(helmDateFormat.parse(release.getUpdated()).getTime());
        } catch (ParseException e) {
            service.setStartedAt(0);
        }
        service.setId(release.getName());
        service.setName(release.getName());
        service.setSubtitle(release.getChart());
        service.setType(Service.ServiceType.KUBERNETES);
        try {
            String values = getHelmInstallService(region).getValues(release.getName(), release.getNamespace());
            JsonNode node = new ObjectMapper().readTree(values);
            Map<String, String> result = new HashMap<>();
            node.fields().forEachRemaining(currentNode -> mapAppender(result, currentNode, new ArrayList<String>()));
            service.setEnv(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return service;
    }

    private void mapAppender(Map<String, String> result, Map.Entry<String, JsonNode> node, List<String> names) {
        names.add(node.getKey());
        if (node.getValue().isValueNode()) {
            String name = names.stream().collect(joining("."));
            result.put(name, node.getValue().asText());
        } else {
            node.getValue().fields().forEachRemaining(nested -> mapAppender(result, nested, new ArrayList<>(names)));
        }
    }

    private Service getServiceFromRelease(Region region, HelmLs release, String manifest) {
        KubernetesClient client = kubernetesClientProvider.getClientForRegion(region);
        InputStream inputStream = new ByteArrayInputStream(manifest.getBytes(Charset.forName("UTF-8")));
        List<HasMetadata> hasMetadatas = client.load(inputStream).get();
        List<Ingress> ingresses = hasMetadatas.stream().filter(hasMetadata -> hasMetadata instanceof Ingress)
                .map(hasMetadata -> (Ingress) hasMetadata).collect(Collectors.toList());
        // List<Service> services = hasMetadatas.stream().filter(hasMetadata ->
        // hasMetadata instanceof Service).map(hasMetadata -> (Service)
        // hasMetadata).collect(Collectors.toList());
        List<Deployment> deployments = hasMetadatas.stream().filter(hasMetadata -> hasMetadata instanceof Deployment)
                .map(hasMetadata -> (Deployment) hasMetadata).collect(Collectors.toList());
        Service service = new Service();
        List<String> urls = new ArrayList<>();
        for (Ingress ingress : ingresses) {
            List<String> listHost = ingress.getSpec().getTls().stream().map(tls -> tls.getHosts())
                    .collect(Collectors.toList()).stream().flatMap(x -> x.stream()).collect(Collectors.toList());
            listHost = listHost.stream().map(host -> {
                if (!host.startsWith("http")) {
                    return "https://" + host;
                }
                return host;
            }).collect(Collectors.toList());
            urls.addAll(listHost);
        }
        service.setUrls(urls);
        Map<String, String> labels = deployments.get(0).getMetadata().getLabels();
        service.setLogo(labels.get("ONYXIA_LOGO"));
        service.setLabels(labels);
        Map<String, Quantity> resources = deployments.get(0).getSpec().getTemplate().getSpec().getContainers().get(0)
                .getResources().getLimits();
        if (resources != null) {

            if (resources.containsKey("memory")) {
                service.setMem(Integer.valueOf(resources.get("memory").getAmount()));
            }

            if (resources.containsKey("cpu")) {
                service.setCpus(Integer.valueOf(resources.get("cpu").getAmount()));
            }
        }
        service.setInstances(deployments.get(0).getSpec().getReplicas());

        service.setTasks(client.pods().inNamespace(release.getNamespace()).withLabel("app.kubernetes.io/instance", release.getName()).list().getItems().stream().map(pod -> {
            Task task = new Task();
            task.setId(pod.getMetadata().getName());
            TaskStatus status = new TaskStatus();
            status.setStatus(pod.getStatus().getPhase());
            status.setReason(pod.getStatus().getContainerStatuses().stream()
                    .filter(cstatus -> cstatus.getState().getWaiting() != null)
                    .map(cstatus -> cstatus.getState().getWaiting().getReason())
            .findFirst().orElse(null));
            task.setStatus(status);
            return task;
        }).collect(Collectors.toList()));

        EventList eventList = client.events().inNamespace(release.getNamespace()).list();
        List<Event> events = eventList.getItems().stream().filter(event -> event.getInvolvedObject().getName().contains(release.getName())).map(event -> {
            Event newEvent = new Event();
            newEvent.setMessage(event.getMessage());
            try {
                // TODO : use kubernetes time format instead of helm
                newEvent.setTimestamp(helmDateFormat.parse(event.getEventTime().getTime()).getTime());
            }
            catch (Exception e) {

            }
            return newEvent;
        }).collect(Collectors.toList());
        service.setEvents(events);

        return service;
    }

    @NotNull
    private String determineNamespace(Region region, String namespacePrefix,User user) {
        KubernetesService.Owner owner = new KubernetesService.Owner();
        owner.setId(user.getIdep());
        owner.setType(KubernetesService.Owner.OwnerType.USER);
        String namespaceId = namespacePrefix + owner.getId();
        // If namespace is not present, create it
        if (kubernetesService.getNamespaces(region, owner).stream()
                .filter(namespace -> namespace.getMetadata().getName().equalsIgnoreCase(namespaceId)).count() == 0) {
            kubernetesService.createNamespace(region, namespaceId, owner);
        }
        return namespaceId;
    }

    private Service.ServiceStatus findAppStatus(HelmLs release) {
        if (release.getStatus().equals("deployed")) {
            return Service.ServiceStatus.RUNNING;
        } else if (release.getStatus().equals("pending")) {
            return Service.ServiceStatus.DEPLOYING;
        } else {
            return Service.ServiceStatus.STOPPED;
        }
    }


}
