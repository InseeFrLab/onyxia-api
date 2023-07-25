package fr.insee.onyxia.api.services.impl;

import fr.insee.onyxia.model.region.Region;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class ServiceUrlResolver {
    static List<String> getServiceUrls(Region region, String manifest, KubernetesClient client) {
        Region.Expose expose = region.getServices().getExpose();
        boolean isIstioEnabled = expose.getIstio() != null && expose.getIstio().isEnabled();
        boolean isServiceExposed = expose.getIngress() || expose.getRoute() || isIstioEnabled;
        if (!isServiceExposed) {
            return List.of();
        }

        List<HasMetadata> hasMetadata;
        try (InputStream inputStream = new ByteArrayInputStream(manifest.getBytes(StandardCharsets.UTF_8))) {
            hasMetadata = client.load(inputStream).items();
        } catch (IOException e) {
            throw new RuntimeException("Exception during loading manifest", e);
        }

        var urls = new ArrayList<String>();

        if (expose.getIngress()) {
            List<Ingress> ingresses = getResourceOfType(hasMetadata, Ingress.class).toList();

            for (Ingress ingress : ingresses) {
                try {
                    urls.addAll(
                            ingress.getSpec().getRules()
                                    .stream()
                                    .flatMap(rule ->
                                            rule.getHttp().getPaths()
                                                    .stream()
                                                    .map(path -> rule.getHost() + path.getPath())
                                    )
                                    .toList());
                } catch (Exception e) {
                    System.out.println(
                            "Warning : could not read urls from ingress "
                                    + ingress.getFullResourceName());
                }
            }
        }

        if (expose.getRoute()) {
            // https://docs.openshift.com/container-platform/4.13/rest_api/network_apis/route-route-openshift-io-v1.html#status-ingress
            // https://docs.openshift.com/container-platform/4.11/networking/routes/route-configuration.html
            List<GenericKubernetesResource> routes =
                    getResourceOfType(hasMetadata, "route.openshift.io/v1", "Route").toList();

            for (GenericKubernetesResource resource : routes) {
                try {
                    urls.add(resource.get("spec", "host"));
                } catch (Exception e) {
                    System.out.println(
                            "Warning : could not read urls from OpenShift Route "
                                    + resource.getFullResourceName());
                }
            }
        }

        if (isIstioEnabled) {
            List<GenericKubernetesResource> virtualServices =
                    getResourceOfType(hasMetadata, "networking.istio.io/", "VirtualService").toList();

            for (GenericKubernetesResource resource : virtualServices) {
                try {
                    // For now we assume we have a simple VirtualService with no routing, thus the hosts are also  the URL.
                    // One should consider to add support for 'spec/http[*]/match[*]/uri/prefix'
                    urls.addAll(resource.get("spec", "hosts"));
                } catch (Exception e) {
                    System.out.println(
                            "Warning : could not read urls from Istio Virtual Service "
                                    + resource.getFullResourceName());
                }
            }
        }

        // Ensure every URL start with http-prefix
        return urls.stream()
                .map(url -> url.startsWith("http") ? url : "https://" + url)
                .toList();
    }

    private static <T extends HasMetadata> Stream<T> getResourceOfType(List<HasMetadata> resourcesStream, Class<T> type) {
        return resourcesStream
                .stream()
                .filter(type::isInstance)
                .map(type::cast);
    }

    private static Stream<GenericKubernetesResource> getResourceOfType(List<HasMetadata> resourcesStream, String apiVersionPrefix, String kind) {
        return getResourceOfType(resourcesStream, GenericKubernetesResource.class)
                .filter(resource -> resource.getApiVersion().startsWith(apiVersionPrefix))
                .filter(resource -> resource.getKind().equalsIgnoreCase(kind));
    }
}
