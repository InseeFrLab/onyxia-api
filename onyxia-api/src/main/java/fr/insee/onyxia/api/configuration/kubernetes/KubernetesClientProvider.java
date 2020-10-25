package fr.insee.onyxia.api.configuration.kubernetes;

import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.region.Region;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesClientProvider {

    /**
     * This returns the root client which has extended permissions. Currently cluster-admin.
     * User calls should be done using the userClient which only has user permissions.
     * @param region
     * @return
     */
    public KubernetesClient getRootClient(Region region) {
        Config config = getDefaultConfiguration(region).build();
        return new DefaultKubernetesClient(config);
    }

    public KubernetesClient getUserClient(Region region, User user) {
        Config config = getDefaultConfiguration(region).build();
        String username = user.getIdep();
        if (region.getServices().getUsernamePrefix() != null) {
            username = region.getServices().getUsernamePrefix()+user.getIdep();
        }
        config.setImpersonateUsername(username);
        config.setImpersonateGroups(null);
        return new DefaultKubernetesClient(config);
    }

    private ConfigBuilder getDefaultConfiguration(Region region) {
        ConfigBuilder configBuilder = new ConfigBuilder();
        if (region.getServices().getServer() != null && region.getServices().getServer().getUrl() != null) {
            configBuilder.withMasterUrl(region.getServices().getServer().getUrl() );
        }

        if (region.getServices().getServer() != null) {
            Region.Auth auth = region.getServices().getServer().getAuth();
            if (auth != null) {
                if (StringUtils.isNotEmpty(auth.getToken())) {
                    configBuilder.withOauthToken(auth.getToken());
                }
                if (StringUtils.isNotEmpty(auth.getUsername()) && StringUtils.isNotEmpty(auth.getPassword())) {
                    configBuilder.withUsername(auth.getUsername());
                    configBuilder.withPassword(auth.getPassword());
                }
            }
        }
        return configBuilder;
    }
}