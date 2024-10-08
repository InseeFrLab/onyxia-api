package fr.insee.onyxia.api.services.impl;

import fr.insee.onyxia.model.service.HealthCheckResult;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.apps.DaemonSet;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HelmReleaseHealthResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelmReleaseHealthResolver.class);

    static List<HealthCheckResult> checkHelmReleaseHealth(
            String namespace, String manifest, KubernetesClient kubernetesClient) {
        // Identify the Helm release secret
        List<HasMetadata> resources;
        try (InputStream inputStream =
                new ByteArrayInputStream(manifest.getBytes(StandardCharsets.UTF_8))) {
            resources = kubernetesClient.load(inputStream).items();
        } catch (IOException e) {
            throw new RuntimeException("Exception during loading manifest", e);
        }

        return checkHealth(namespace, resources, kubernetesClient);
    }

    private static List<HealthCheckResult> checkHealth(
            String namespace, List<HasMetadata> resources, KubernetesClient kubernetesClient) {
        List<HealthCheckResult> results = new ArrayList<>();
        for (HasMetadata resource : resources) {
            String name = resource.getMetadata().getName();
            String kind = resource.getKind();
            HealthCheckResult result = new HealthCheckResult();
            result.setName(name);
            result.setKind(kind);
            HealthCheckResult.HealthDetails details = new HealthCheckResult.HealthDetails();
            try {
                switch (kind) {
                    case "Deployment":
                        Deployment deployment =
                                kubernetesClient
                                        .apps()
                                        .deployments()
                                        .inNamespace(namespace)
                                        .withName(name)
                                        .get();
                        if (deployment == null) continue;
                        details.setDesired(deployment.getSpec().getReplicas());
                        // If replicas is 0 then readyReplicas is not defined (and can't be
                        // different from 0 anyway)
                        if (deployment.getStatus().getReplicas() > 0
                                && deployment.getStatus().getReadyReplicas() != null) {
                            details.setReady(deployment.getStatus().getReadyReplicas());
                        }
                        break;
                    case "StatefulSet":
                        StatefulSet statefulset =
                                kubernetesClient
                                        .apps()
                                        .statefulSets()
                                        .inNamespace(namespace)
                                        .withName(name)
                                        .get();
                        if (statefulset == null) continue;
                        details.setDesired(statefulset.getSpec().getReplicas());
                        // If replicas is 0 then readyReplicas is not defined (and can't be
                        // different from 0 anyway)
                        if (statefulset.getStatus().getReplicas() > 0
                                && statefulset.getStatus().getReadyReplicas() != null) {
                            details.setReady(statefulset.getStatus().getReadyReplicas());
                        }
                        break;
                    case "DaemonSet":
                        DaemonSet daemonSet =
                                kubernetesClient
                                        .apps()
                                        .daemonSets()
                                        .inNamespace(namespace)
                                        .withName(name)
                                        .get();
                        if (daemonSet == null) continue;
                        details.setDesired(daemonSet.getStatus().getDesiredNumberScheduled());
                        // If replicas is 0 then readyReplicas is not defined (and can't be
                        // different from 0 anyway)
                        if (daemonSet.getStatus().getNumberAvailable() > 0
                                && daemonSet.getStatus().getNumberReady() != null) {
                            details.setReady(daemonSet.getStatus().getNumberReady());
                        }
                        break;
                    default:
                        continue;
                }
            } catch (Exception e) {
                LOGGER.warn(
                        "Could not retrieve health status from resource kind {} name {} ",
                        resource.getKind(),
                        resource.getMetadata().getName(),
                        e);
            }
            result.setDetails(details);
            result.setHealthy(details.getReady() >= details.getDesired());
            results.add(result);
        }
        return results;
    }
}
