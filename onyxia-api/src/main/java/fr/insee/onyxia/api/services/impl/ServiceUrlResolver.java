package fr.insee.onyxia.api.services.impl;

import fr.insee.onyxia.model.region.Region;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.gatewayapi.v1.HTTPRoute;
import io.fabric8.kubernetes.api.model.gatewayapi.v1.HTTPRouteMatch;
import io.fabric8.kubernetes.api.model.gatewayapi.v1.HTTPRouteRule;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServiceUrlResolver {

    private ServiceUrlResolver() {}

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceUrlResolver.class);

    static List<String> getServiceUrls(Region region, String manifest, KubernetesClient client) {
        Region.Expose expose = region.getServices().getExpose();
        boolean isIstioEnabled = expose.getIstio() != null && expose.getIstio().isEnabled();
        boolean isHttpRouteEnabled = expose.getHttpRoute().isEnabled();
        boolean isServiceExposed =
                expose.getIngress() || expose.getRoute() || isIstioEnabled || isHttpRouteEnabled;
        if (!isServiceExposed) {
            return List.of();
        }

        List<HasMetadata> hasMetadata;
        try (InputStream inputStream =
                new ByteArrayInputStream(manifest.getBytes(StandardCharsets.UTF_8))) {
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
                            ingress.getSpec().getRules().stream()
                                    .flatMap(
                                            rule ->
                                                    rule.getHttp().getPaths().stream()
                                                            .map(
                                                                    path ->
                                                                            rule.getHost()
                                                                                    + path
                                                                                            .getPath()))
                                    .toList());
                } catch (Exception e) {
                    LOGGER.warn(
                            "Could not read urls from ingress {}", ingress.getFullResourceName());
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
                    LOGGER.warn(
                            "Could not read urls from OpenShift Route {}",
                            resource.getFullResourceName());
                }
            }
        }

        if (isIstioEnabled) {
            List<GenericKubernetesResource> virtualServices =
                    getResourceOfType(hasMetadata, "networking.istio.io/", "VirtualService")
                            .toList();

            for (GenericKubernetesResource resource : virtualServices) {
                try {
                    // For now we assume we have a simple VirtualService with no routing, thus the
                    // hosts are also  the URL.
                    // One should consider to add support for 'spec/http[*]/match[*]/uri/prefix'
                    urls.addAll(resource.get("spec", "hosts"));
                } catch (Exception e) {
                    LOGGER.warn(
                            "Could not read urls from Istio Virtual Service {}",
                            resource.getFullResourceName());
                }
            }
        }

        if (isHttpRouteEnabled) {
            List<HTTPRoute> httpRoutes = getResourceOfType(hasMetadata, HTTPRoute.class).toList();

            for (HTTPRoute httpRoute : httpRoutes) {
                try {
                    urls.addAll(getHttpRouteUrls(httpRoute));
                } catch (Exception e) {
                    LOGGER.warn(
                            "Could not read urls from HTTPRoute {}",
                            httpRoute.getFullResourceName());
                }
            }
        }

        // Ensure every URL start with http-prefix
        return urls.stream().map(url -> url.startsWith("http") ? url : "https://" + url).toList();
    }

    private static List<String> getHttpRouteUrls(HTTPRoute httpRoute) {
        List<String> hostnames =
                httpRoute.getSpec() == null || httpRoute.getSpec().getHostnames() == null
                        ? List.of()
                        : httpRoute.getSpec().getHostnames();

        if (hostnames.isEmpty()) {
            LOGGER.warn(
                    "Could not determine urls from HTTPRoute {} because spec.hostnames is empty",
                    httpRoute.getFullResourceName());
            return List.of();
        }

        List<String> paths = getHttpRoutePaths(httpRoute);

        return hostnames.stream()
                .flatMap(hostname -> paths.stream().map(path -> hostname + normalizePath(path)))
                .toList();
    }

    private static List<String> getHttpRoutePaths(HTTPRoute httpRoute) {
        if (httpRoute.getSpec() == null
                || httpRoute.getSpec().getRules() == null
                || httpRoute.getSpec().getRules().isEmpty()) {
            return List.of("/");
        }

        LinkedHashSet<String> paths = new LinkedHashSet<>();
        boolean hasImplicitRootMatch = false;

        for (HTTPRouteRule rule : httpRoute.getSpec().getRules()) {
            if (rule.getMatches() == null || rule.getMatches().isEmpty()) {
                hasImplicitRootMatch = true;
                continue;
            }

            for (HTTPRouteMatch match : rule.getMatches()) {
                if ((match.getHeaders() != null && !match.getHeaders().isEmpty())
                        || (match.getQueryParams() != null && !match.getQueryParams().isEmpty())
                        || match.getMethod() != null) {
                    continue;
                }

                if (match.getPath() == null) {
                    continue;
                }

                String pathType = match.getPath().getType();

                if (pathType != null && !pathType.equals("PathPrefix") && !pathType.equals("Exact")) {
                    continue;
                }

                String pathValue = match.getPath().getValue();

                if (pathValue == null || pathValue.isBlank()) {
                    continue;
                }

                paths.add(normalizePath(pathValue));
            }
        }

        if (!paths.isEmpty()) {
            return List.copyOf(paths);
        }

        return hasImplicitRootMatch ? List.of("/") : List.of();
    }

    private static String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }

        return path.startsWith("/") ? path : "/" + path;
    }

    private static <T extends HasMetadata> Stream<T> getResourceOfType(
            List<HasMetadata> resourcesStream, Class<T> type) {
        return resourcesStream.stream().filter(type::isInstance).map(type::cast);
    }

    private static Stream<GenericKubernetesResource> getResourceOfType(
            List<HasMetadata> resourcesStream, String apiVersionPrefix, String kind) {
        return getResourceOfType(resourcesStream, GenericKubernetesResource.class)
                .filter(resource -> resource.getApiVersion().startsWith(apiVersionPrefix))
                .filter(resource -> resource.getKind().equalsIgnoreCase(kind));
    }
}
