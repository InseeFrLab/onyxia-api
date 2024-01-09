package fr.insee.onyxia.api.services.impl;

import static fr.insee.onyxia.api.util.TestUtils.getClassPathResource;
import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.insee.onyxia.model.region.Region;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.EnableKubernetesMockClient;
import java.util.List;
import org.junit.jupiter.api.Test;

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
                getClassPathResource(ISTIO_VIRTUAL_SERVICE_MANIFEST_PATH)
                        + YAML_LINE_BREAK
                        + getClassPathResource(INGRESS_MANIFEST_PATH)
                        + YAML_LINE_BREAK
                        + getClassPathResource(OPENSHIFT_ROUTE_MANIFEST_PATH);

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
                getClassPathResource(ISTIO_VIRTUAL_SERVICE_MANIFEST_PATH)
                        + YAML_LINE_BREAK
                        + getClassPathResource(INGRESS_MANIFEST_PATH)
                        + YAML_LINE_BREAK
                        + getClassPathResource(OPENSHIFT_ROUTE_MANIFEST_PATH);

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
        var manifest = getClassPathResource(INGRESS_MANIFEST_PATH);

        List<String> urls = ServiceUrlResolver.getServiceUrls(region, manifest, kubernetesClient);
        assertEquals(List.of("https://jupyter-python-3574-0.example.com/"), urls);
    }

    @Test
    void istio_virtual_service_should_be_included_in_urls() {
        Region region = getRegionNoExposed();
        region.getServices().getExpose().getIstio().setEnabled(true);
        var manifest = getClassPathResource(ISTIO_VIRTUAL_SERVICE_MANIFEST_PATH);

        List<String> urls = ServiceUrlResolver.getServiceUrls(region, manifest, kubernetesClient);
        assertEquals(List.of("https://jupyter-python-3574-0.example.com"), urls);
    }

    @Test
    void openshift_route_should_be_included_in_urls() {
        Region region = getRegionNoExposed();
        region.getServices().getExpose().setRoute(true);
        var manifest = getClassPathResource(OPENSHIFT_ROUTE_MANIFEST_PATH);

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
