package fr.insee.onyxia.api.configuration.kubernetes;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubClient {

    @Bean
    public KubernetesClient kubernetesClient() {
        return new DefaultKubernetesClient();
    }
}