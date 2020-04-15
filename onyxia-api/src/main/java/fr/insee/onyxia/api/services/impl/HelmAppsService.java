package fr.insee.onyxia.api.services.impl;

import fr.insee.onyxia.api.services.AppsService;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.service.Service;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.github.inseefrlab.helmwrapper.model.HelmLs;
import io.github.inseefrlab.helmwrapper.service.HelmInstallService;
import mesosphere.marathon.client.model.v2.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Qualifier("Helm")
public class HelmAppsService implements AppsService {

    @Autowired
    HelmInstallService helm;


    @Override
    public Group getGroups(String id) {
        return null;
    }

    @Override
    public List<Service> getUserServices(User user) throws InterruptedException, TimeoutException, IOException {
        List<HelmLs> installedCharts = Arrays.asList(helm.listChartInstall(user.getIdep()));
        List<Service> services = installedCharts.stream().map(release -> helm.getRelease(release.getName(), release.getNamespace())).map(release -> getServiceFromRelease(release)).collect(Collectors.toList());
        return services;
    }

    private Service getServiceFromRelease(String description) {
        KubernetesClient client = new DefaultKubernetesClient();
        InputStream inputStream = new ByteArrayInputStream(description.getBytes(Charset.forName("UTF-8")));
        List<HasMetadata> hasMetadatas = client.load(inputStream).get();
        //List<Ingress> ingresses = hasMetadatas.stream().filter(hasMetadata -> hasMetadata instanceof Ingress).map(hasMetadata -> (Ingress) hasMetadata).collect(Collectors.toList());
        //List<Service> services = hasMetadatas.stream().filter(hasMetadata -> hasMetadata instanceof Service).map(hasMetadata -> (Service) hasMetadata).collect(Collectors.toList());
        List<Deployment> deployments = hasMetadatas.stream().filter(hasMetadata -> hasMetadata instanceof Deployment).map(hasMetadata -> (Deployment) hasMetadata).collect(Collectors.toList());
        Service service = new Service();
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
        service.setTitle(deployments.get(0).getMetadata().getName());
        service.setId(deployments.get(0).getMetadata().getName());
        return service;
    }
}
