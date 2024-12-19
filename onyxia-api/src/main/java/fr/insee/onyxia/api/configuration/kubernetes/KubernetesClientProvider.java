package fr.insee.onyxia.api.configuration.kubernetes;

import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.region.Region;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesClientProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(KubernetesClientProvider.class);

    private static Map<String, KubernetesClient> rootKubernetesClientCache = new HashMap<>();

    private static KubernetesClient userKubernetesClientCache = null;

    /**
     * This returns the root client which has extended permissions. Currently cluster-admin. User
     * calls should be done using the userClient which only has user permissions.
     *
     * @param region
     * @return
     */
    public synchronized KubernetesClient getRootClient(Region region) {
        if (!rootKubernetesClientCache.containsKey(region.getId())) {
            final Config config = getDefaultConfiguration(region).build();
            rootKubernetesClientCache.put(
                    region.getId(), new KubernetesClientBuilder().withConfig(config).build());
        }
        return rootKubernetesClientCache.get(region.getId());
    }

    public KubernetesClient getUserClient(Region region, User user) {
        // In case of SERVICEACCOUNT authentication, we can safely mutualize and use a single
        // KubernetesClient
        if (region.getServices().getAuthenticationMode()
                == Region.Services.AuthenticationMode.SERVICEACCOUNT) {
            if (userKubernetesClientCache != null) {
                return userKubernetesClientCache;
            }
            Config config = getDefaultConfiguration(region).build();
            KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build();
            userKubernetesClientCache = client;
            return client;
        }
        final Config config = getDefaultConfiguration(region).build();
        String username = user.getIdep();
        if (region.getServices().getUsernamePrefix() != null) {
            username = region.getServices().getUsernamePrefix() + user.getIdep();
        }

        if (region.getServices().getAuthenticationMode()
                == Region.Services.AuthenticationMode.IMPERSONATE) {
            config.setImpersonateUsername(username);
            config.setImpersonateGroups(null);
        }

        if (region.getServices().getAuthenticationMode()
                == Region.Services.AuthenticationMode.TOKEN_PASSTHROUGH) {
            config.setOauthToken((String) user.getAttributes().get("access_token"));
        }

        return new KubernetesClientBuilder().withConfig(config).build();
    }

    private ConfigBuilder getDefaultConfiguration(Region region) {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        if (region.getServices().getServer() != null
                && region.getServices().getServer().getUrl() != null) {
            configBuilder.withMasterUrl(region.getServices().getServer().getUrl());
        }

        if (region.getServices().getServer() != null) {
            final Region.Auth auth = region.getServices().getServer().getAuth();
            if (auth != null) {
                if (StringUtils.isNotEmpty(auth.getToken())) {
                    configBuilder.withOauthToken(auth.getToken());
                }
                if (StringUtils.isNotEmpty(auth.getUsername())
                        && StringUtils.isNotEmpty(auth.getPassword())) {
                    configBuilder.withUsername(auth.getUsername());
                    configBuilder.withPassword(auth.getPassword());
                }
            }
        }
        return configBuilder;
    }
}
