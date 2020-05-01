package fr.insee.onyxia.api.configuration.kubernetes;

import fr.insee.onyxia.model.region.Region;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesClientProvider {

    @Bean
    public KubernetesClient defaultKubernetesClient() {
        return new DefaultKubernetesClient();
    }

    public KubernetesClient getClientForRegion(Region region) {
        ConfigBuilder configBuilder = new ConfigBuilder();
        if (region.getServerUrl() != null) {
            configBuilder.withMasterUrl(region.getServerUrl());
        }
        if (region.getAuth() != null) {
            if (StringUtils.isNotEmpty(region.getAuth().getToken())) {
                configBuilder.withOauthToken(region.getAuth().getToken());
            }
            if (StringUtils.isNotEmpty(region.getAuth().getUsername()) && StringUtils.isNotEmpty(region.getAuth().getPassword())) {
                configBuilder.withUsername(region.getAuth().getUsername());
                configBuilder.withPassword(region.getAuth().getPassword());
            }
        }
        Config config = configBuilder.build();
        KubernetesClient client = new DefaultKubernetesClient(config);
        return client;
    }
}