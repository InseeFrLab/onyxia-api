package fr.insee.onyxia.api.services.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.insee.onyxia.model.region.Region;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.EnableKubernetesMockClient;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

@EnableKubernetesMockClient
class ServiceUrlResolverTest {

    private KubernetesClient kubernetesClient;
    private final String ISTIO_VIRTUAL_SERVICE_MANIFEST_PATH =
            "kubernetes-manifest/istio-virtualservice.yaml";
    private final String INGRESS_MANIFEST_PATH = "kubernetes-manifest/k8s-ingress.yaml";
    private final String OPENSHIFT_ROUTE_MANIFEST_PATH = "kubernetes-manifest/openshift-route.yaml";
    private final String YAML_LINE_BREAK = "\n---\n";

    @Test
    void urls_should_be_empty() {
        Region region = getRegionNoExposed();
        var allManifest =
                getManifest(ISTIO_VIRTUAL_SERVICE_MANIFEST_PATH)
                        + "\n---\n"
                        + getManifest(INGRESS_MANIFEST_PATH)
                        + "\n---\n"
                        + getManifest(OPENSHIFT_ROUTE_MANIFEST_PATH);

        List<String> urls =
                ServiceUrlResolver.getServiceUrls(region, allManifest, kubernetesClient);
        assertEquals(List.of(), urls);
    }

    @Test
    void urls_should_be_present_for_all_ingress_types() {
        Region region = getRegionNoExposed();
        region.getServices().getExpose().setIngress(true);
        region.getServices().getExpose().setRoute(true);
        region.getServices().getExpose().getIstio().setEnabled(true);
        var allManifest =
                getManifest(ISTIO_VIRTUAL_SERVICE_MANIFEST_PATH)
                        + YAML_LINE_BREAK
                        + getManifest(INGRESS_MANIFEST_PATH)
                        + YAML_LINE_BREAK
                        + getManifest(OPENSHIFT_ROUTE_MANIFEST_PATH);

        List<String> urls =
                ServiceUrlResolver.getServiceUrls(region, allManifest, kubernetesClient);
        List<String> expected =
                List.of(
                        "https://jupyter-python-3574-0.example.com/",
                        "https://hello-openshift.example.com",
                        "https://jupyter-python-3574-0.example.com");
        assertEquals(expected, urls);
    }

    @Test
    void k8s_ingress_should_be_included_in_urls() {
        Region region = getRegionNoExposed();
        region.getServices().getExpose().setIngress(true);
        var manifest = getManifest(INGRESS_MANIFEST_PATH);

        List<String> urls = ServiceUrlResolver.getServiceUrls(region, manifest, kubernetesClient);
        assertEquals(List.of("https://jupyter-python-3574-0.example.com/"), urls);
    }

    private String getManifest(String manifestClassPath) {
        try {
            return new String(
                    new ClassPathResource(manifestClassPath).getInputStream().readAllBytes(),
                    UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void istio_virtual_service_should_be_included_in_urls() {
        Region region = getRegionNoExposed();
        region.getServices().getExpose().getIstio().setEnabled(true);
        var manifest = "";

        manifest = getManifest(ISTIO_VIRTUAL_SERVICE_MANIFEST_PATH);

        List<String> urls = ServiceUrlResolver.getServiceUrls(region, manifest, kubernetesClient);
        assertEquals(List.of("https://jupyter-python-3574-0.example.com"), urls);
    }

    @Test
    void openshift_route_should_be_included_in_urls() {
        Region region = getRegionNoExposed();
        region.getServices().getExpose().setRoute(true);
        var manifest = "";

        manifest = getManifest(OPENSHIFT_ROUTE_MANIFEST_PATH);

        List<String> urls = ServiceUrlResolver.getServiceUrls(region, manifest, kubernetesClient);
        assertEquals(List.of("https://hello-openshift.example.com"), urls);
    }

    private static Region getRegionNoExposed() {
        Region.Expose expose = new Region.Expose();
        expose.setIngress(false);
        expose.setRoute(false);

        Region.IstioIngress istio = new Region.IstioIngress();
        istio.setEnabled(false);
        expose.setIstio(istio);

        Region.Services services = new Region.Services();
        services.setExpose(expose);

        var region = new Region();
        region.setServices(services);
        return region;
    }
}
