import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;

import fr.insee.onyxia.model.service.*;

@Service
public final class HelmReleaseHealthResolver {

    static List<HealthCheckResult> checkHelmReleaseHealth(String namespace, String manifest, KubernetesClient kubernetesClient) {
        // Identify the Helm release secret
        List<HasMetadata> resources;
        try (InputStream inputStream =
                new ByteArrayInputStream(manifest.getBytes(StandardCharsets.UTF_8))) {
            resources = client.load(inputStream).items();
        } catch (IOException e) {
            throw new RuntimeException("Exception during loading manifest", e);
        }

        // Check the health of all resources and collect detailed results
        List<HealthCheckResult> results = new ArrayList<>();
        results.addAll(checkPodsHealth(resources));
        results.addAll(checkDeploymentsHealth(namespace, resources, kubernetesClient));
        results.addAll(checkStatefulSetsHealth(namespace, resources, kubernetesClient));
        results.addAll(checkDaemonSetsHealth(namespace, resources, kubernetesClient));
        results.addAll(checkReplicaSetsHealth(namespace, resources, kubernetesClient));

        return results;
    }

    private static List<HealthCheckResult> checkPodsHealth(List<HasMetadata> resources, KubernetesClient kubernetesClient) {
        List<HealthCheckResult> results = new ArrayList<>();
        List<Pod> pods = resources.stream()
                .filter(resource -> resource instanceof Pod)
                .map(resource -> (Pod) resource)
                .collect(Collectors.toList());

        for (Pod pod : pods) {
            String podPhase = pod.getStatus().getPhase();
            boolean healthy = "Running".equals(podPhase);
            Map<String, Object> details = new HashMap<>();
            details.put("phase", podPhase);

            results.add(new HealthCheckResult(healthy, pod.getMetadata().getName(), "Pod", details));
        }
        return results;
    }

    private static List<HealthCheckResult> checkDeploymentsHealth(String namespace, List<HasMetadata> resources, KubernetesClient kubernetesClient) {
        List<HealthCheckResult> results = new ArrayList<>();
        List<HasMetadata> deployments = resources.stream()
                .filter(resource -> "Deployment".equals(resource.getKind()))
                .collect(Collectors.toList());

        for (HasMetadata deployment : deployments) {
            int availableReplicas = kubernetesClient.apps().deployments()
                    .inNamespace(namespace).withName(deployment.getMetadata().getName())
                    .get().getStatus().getAvailableReplicas();

            int desiredReplicas = kubernetesClient.apps().deployments()
                    .inNamespace(namespace).withName(deployment.getMetadata().getName())
                    .get().getSpec().getReplicas();

            boolean healthy = availableReplicas >= desiredReplicas;
            Map<String, Object> details = new HashMap<>();
            details.put("availableReplicas", availableReplicas);
            details.put("desiredReplicas", desiredReplicas);

            results.add(new HealthCheckResult(healthy, deployment.getMetadata().getName(), "Deployment", details));
        }
        return results;
    }

    private static List<HealthCheckResult> checkStatefulSetsHealth(String namespace, List<HasMetadata> resources, KubernetesClient kubernetesClient) {
        List<HealthCheckResult> results = new ArrayList<>();
        List<HasMetadata> statefulSets = resources.stream()
                .filter(resource -> "StatefulSet".equals(resource.getKind()))
                .collect(Collectors.toList());

        for (HasMetadata statefulSet : statefulSets) {
            int readyReplicas = kubernetesClient.apps().statefulSets()
                    .inNamespace(namespace).withName(statefulSet.getMetadata().getName())
                    .get().getStatus().getReadyReplicas();

            int desiredReplicas = kubernetesClient.apps().statefulSets()
                    .inNamespace(namespace).withName(statefulSet.getMetadata().getName())
                    .get().getSpec().getReplicas();

            boolean healthy = readyReplicas >= desiredReplicas;
            Map<String, Object> details = new HashMap<>();
            details.put("readyReplicas", readyReplicas);
            details.put("desiredReplicas", desiredReplicas);

            results.add(new HealthCheckResult(healthy, statefulSet.getMetadata().getName(), "StatefulSet", details));
        }
        return results;
    }

    private static List<HealthCheckResult> checkDaemonSetsHealth(String namespace, List<HasMetadata> resources, KubernetesClient kubernetesClient) {
        List<HealthCheckResult> results = new ArrayList<>();
        List<HasMetadata> daemonSets = resources.stream()
                .filter(resource -> "DaemonSet".equals(resource.getKind()))
                .collect(Collectors.toList());

        for (HasMetadata daemonSet : daemonSets) {
            int desiredNumberScheduled = kubernetesClient.apps().daemonSets()
                    .inNamespace(namespace).withName(daemonSet.getMetadata().getName())
                    .get().getStatus().getDesiredNumberScheduled();

            int numberAvailable = kubernetesClient.apps().daemonSets()
                    .inNamespace(namespace).withName(daemonSet.getMetadata().getName())
                    .get().getStatus().getNumberAvailable();

            boolean healthy = numberAvailable >= desiredNumberScheduled;
            Map<String, Object> details = new HashMap<>();
            details.put("desiredNumberScheduled", desiredNumberScheduled);
            details.put("numberAvailable", numberAvailable);

            results.add(new HealthCheckResult(healthy, daemonSet.getMetadata().getName(), "DaemonSet", details));
        }
        return results;
    }

    private static List<HealthCheckResult> checkReplicaSetsHealth(String namespace, List<HasMetadata> resources, KubernetesClient kubernetesClient) {
        List<HealthCheckResult> results = new ArrayList<>();
        List<HasMetadata> replicaSets = resources.stream()
                .filter(resource -> "ReplicaSet".equals(resource.getKind()))
                .collect(Collectors.toList());

        for (HasMetadata replicaSet : replicaSets) {
            int readyReplicas = kubernetesClient.apps().replicaSets()
                    .inNamespace(namespace).withName(replicaSet.getMetadata().getName())
                    .get().getStatus().getReadyReplicas();

            int desiredReplicas = kubernetesClient.apps().replicaSets()
                    .inNamespace(namespace).withName(replicaSet.getMetadata().getName())
                    .get().getSpec().getReplicas();

            boolean healthy = readyReplicas >= desiredReplicas;
            Map<String, Object> details = new HashMap<>();
            details.put("readyReplicas", readyReplicas);
            details.put("desiredReplicas", desiredReplicas);

            results.add(new HealthCheckResult(healthy, replicaSet.getMetadata().getName(), "ReplicaSet", details));
        }
        return results;
    }
}
