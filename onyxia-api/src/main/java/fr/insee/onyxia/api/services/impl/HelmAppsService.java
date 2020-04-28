package fr.insee.onyxia.api.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.onyxia.api.services.AppsService;
import fr.insee.onyxia.api.services.control.AdmissionControllerHelm;
import fr.insee.onyxia.api.services.impl.kubernetes.KubernetesService;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Package;
import fr.insee.onyxia.model.dto.CreateServiceDTO;
import fr.insee.onyxia.model.dto.ServicesListing;
import fr.insee.onyxia.model.service.Service;
import fr.insee.onyxia.model.service.Task;
import fr.insee.onyxia.model.service.TaskStatus;
import fr.insee.onyxia.model.service.UninstallService;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
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
import org.springframework.beans.factory.annotation.Value;

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
    HelmInstallService helm;

    @Autowired
    private KubernetesService kubernetesService;

    @Autowired
    @Qualifier("helm")
    ObjectMapper mapperHelm;

    @Value("${kubernetes.namespace.prefix}")
    private String KUBERNETES_NAMESPACE_PREFIX;

    @Autowired
    private List<AdmissionControllerHelm> admissionControllers;

    private SimpleDateFormat helmDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final Logger LOGGER = LoggerFactory.getLogger(HelmAppsService.class);

    public Collection<Object> installApp(CreateServiceDTO requestDTO, boolean isGroup, String catalogId, Package pkg,
            User user, Map<String, Object> fusion) throws IOException, TimeoutException, InterruptedException {

        // we inject values here
        admissionControllers.stream().forEach(controller -> controller.validateContract(pkg, fusion, user));
        File values = File.createTempFile("values", ".yaml");
        mapperHelm.writeValue(values, fusion);
        String namespaceId = determineNamespace(user);
        HelmInstaller res = helm.installChart(catalogId + "/" + pkg.getName(), namespaceId, requestDTO.isDryRun(),
                values);
        values.delete();
        return List.of(res.getManifest());
    }

    @Override
    public CompletableFuture<ServicesListing> getUserServices(User user) throws IOException, IllegalAccessException {
        return getUserServices(user, null);
    }

    @Override
    public CompletableFuture<ServicesListing> getUserServices(User user, String groupId)
            throws IOException, IllegalAccessException {
        if (groupId != null) {
            LOGGER.debug("STUB : group listing is currently not supported on helm");
            return CompletableFuture.completedFuture(new ServicesListing());
        }
        List<HelmLs> installedCharts = null;
        try {
            installedCharts = Arrays.asList(helm.listChartInstall(KUBERNETES_NAMESPACE_PREFIX + user.getIdep()));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(new ServicesListing());
        }
        List<Service> services = new ArrayList<>();
        for (HelmLs release : installedCharts) {
            services.add(getHelmApp(release));
        }
        ServicesListing listing = new ServicesListing();
        listing.setApps(services);
        return CompletableFuture.completedFuture(listing);
    }

    @Override
    public String getLogs(User user, String serviceId, String taskId) {
        KubernetesClient client = new DefaultKubernetesClient();
        return client.pods().inNamespace(determineNamespace(user)).withName(taskId).getLog();
    }

    private Service getHelmApp(HelmLs release) {
        String manifest = helm.getManifest(release.getName(), release.getNamespace());
        Service service = getServiceFromRelease(release, manifest);
        service.setStatus(findAppStatus(release));
        try {
            service.setStartedAt(helmDateFormat.parse(release.getUpdated()).getTime());
        } catch (ParseException e) {
            service.setStartedAt(0);
        }
        service.setId(release.getName());
        service.setName(release.getChart());
        service.setType(Service.ServiceType.KUBERNETES);
        try {
            String values = helm.getValues(release.getName(), release.getNamespace());
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

    private Service getServiceFromRelease(HelmLs release, String manifest) {
        KubernetesClient client = new DefaultKubernetesClient();
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

        return service;
    }

    @NotNull
    private String determineNamespace(User user) {
        KubernetesService.Owner owner = new KubernetesService.Owner();
        owner.setId(user.getIdep());
        owner.setType(KubernetesService.Owner.OwnerType.USER);
        String namespaceId = KUBERNETES_NAMESPACE_PREFIX + owner.getId();
        // If namespace is not present, create it
        if (kubernetesService.getNamespaces(owner).stream()
                .filter(namespace -> namespace.getMetadata().getName().equalsIgnoreCase(namespaceId)).count() == 0) {
            kubernetesService.createNamespace(namespaceId, owner);
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

    @Override
    public Service getUserService(User user, String serviceId) throws MultipleServiceFound, ParseException {
        if (serviceId.startsWith("/")) {
            serviceId = serviceId.substring(1);
        }
        HelmLs result = helm.getAppById(serviceId, determineNamespace(user));
        return getHelmApp(result);
    }

    @Override
    public UninstallService destroyService(User user, String serviceId) throws Exception {
        HelmLs appInfo = helm.getAppById(serviceId, determineNamespace(user));
        UninstallService result = new UninstallService();
        result.setId(appInfo.getName());
        result.setVersion(appInfo.getChart());
        int status = helm.uninstaller(serviceId, determineNamespace(user));
        if (status != 0) {
            result.setSuccess(false);
        }
        result.setSuccess(true);
        return result;
    }
}
