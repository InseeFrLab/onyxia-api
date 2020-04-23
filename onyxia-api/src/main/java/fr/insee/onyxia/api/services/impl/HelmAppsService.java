package fr.insee.onyxia.api.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.onyxia.api.services.AppsService;
import fr.insee.onyxia.api.services.impl.kubernetes.KubernetesService;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Package;
import fr.insee.onyxia.model.dto.CreateServiceDTO;
import fr.insee.onyxia.model.service.Service;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.github.inseefrlab.helmwrapper.model.HelmInstaller;
import io.github.inseefrlab.helmwrapper.model.HelmLs;
import io.github.inseefrlab.helmwrapper.service.HelmInstallService;
import org.jetbrains.annotations.NotNull;
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

    private SimpleDateFormat helmDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public Collection<Object> installApp(CreateServiceDTO requestDTO, boolean isGroup, String catalogId, Package pkg,
            User user, Map<String, Object> fusion) throws IOException, TimeoutException, InterruptedException {
        File values = File.createTempFile("values", ".yaml");
        mapperHelm.writeValue(values, fusion);
        String namespaceId = determineNamespace(user);
        HelmInstaller res = helm.installChart(catalogId + "/" + pkg.getName(), namespaceId, requestDTO.isDryRun(),
                values);
        values.delete();
        return List.of(res.getManifest());
    }

    @Override
    public CompletableFuture<List<Service>> getUserServices(User user)
            throws InterruptedException, TimeoutException, IOException, ParseException {
        List<HelmLs> installedCharts = null;
        try {
            installedCharts = Arrays.asList(helm.listChartInstall(KUBERNETES_NAMESPACE_PREFIX + user.getIdep()));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
        List<Service> services = new ArrayList<>();
        for (HelmLs release : installedCharts) {
            String description = helm.getRelease(release.getName(), release.getNamespace());
            Service service = getServiceFromRelease(description);
            service.setStatus(findAppStatus(release));
            service.setStartedAt(helmDateFormat.parse(release.getUpdated()).getTime());
            service.setId(release.getChart());
            service.setName(release.getChart());
            services.add(service);
            service.setType(Service.ServiceType.KUBERNETES);
        }
        return CompletableFuture.completedFuture(services);
    }

    private Service getServiceFromRelease(String description) {
        KubernetesClient client = new DefaultKubernetesClient();
        InputStream inputStream = new ByteArrayInputStream(description.getBytes(Charset.forName("UTF-8")));
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
        return service;
    }

    @NotNull
    private String determineNamespace(User user) {
        KubernetesService.Owner owner = new KubernetesService.Owner();
        owner.setId(user.getIdep());
        owner.setType(KubernetesService.Owner.OwnerType.USER);
        String namespaceId = KUBERNETES_NAMESPACE_PREFIX + owner.getId();
        // If namespace is not present, create it
        if (!kubernetesService.getNamespaces(owner).contains(namespaceId)) {
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
}
