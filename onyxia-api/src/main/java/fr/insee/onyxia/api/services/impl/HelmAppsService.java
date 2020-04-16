package fr.insee.onyxia.api.services.impl;

import fr.insee.onyxia.api.services.AppsService;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.service.Service;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.github.inseefrlab.helmwrapper.model.HelmLs;
import io.github.inseefrlab.helmwrapper.service.HelmInstallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Qualifier("Helm")
public class HelmAppsService implements AppsService {

    @Autowired
    HelmInstallService helm;

    private SimpleDateFormat helmDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public CompletableFuture<List<Service>> getUserServices(User user) throws InterruptedException, TimeoutException, IOException, ParseException {
        List<HelmLs> installedCharts = Arrays.asList(helm.listChartInstall("onyxia"));
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
        List<Ingress> ingresses = hasMetadatas.stream().filter(hasMetadata -> hasMetadata instanceof Ingress).map(hasMetadata -> (Ingress) hasMetadata).collect(Collectors.toList());
        //List<Service> services = hasMetadatas.stream().filter(hasMetadata -> hasMetadata instanceof Service).map(hasMetadata -> (Service) hasMetadata).collect(Collectors.toList());
        List<Deployment> deployments = hasMetadatas.stream().filter(hasMetadata -> hasMetadata instanceof Deployment).map(hasMetadata -> (Deployment) hasMetadata).collect(Collectors.toList());
        Service service = new Service();
        List<String> urls = new ArrayList<>();
        for (Ingress ingress: ingresses) {
            List<String> listHost = ingress.getSpec().getTls().stream().map(tls -> tls.getHosts()).collect(Collectors.toList()).stream().flatMap(x -> x.stream()).collect(Collectors.toList());
            urls.addAll(listHost);
        }
        service.setUrls(urls);
        service.setLabels(deployments.get(0).getMetadata().getLabels());
        Map<String, Quantity> resources = deployments.get(0).getSpec().getTemplate().getSpec().getContainers().get(0).getResources().getLimits();
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
