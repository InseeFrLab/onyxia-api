package fr.insee.onyxia.api.configuration.kubernetes;

import fr.insee.onyxia.api.configuration.SecurityConfig;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.region.Region;
import io.github.inseefrlab.helmwrapper.configuration.HelmConfiguration;
import io.github.inseefrlab.helmwrapper.service.HelmInstallService;
import io.github.inseefrlab.helmwrapper.service.HelmRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HelmClientProvider {

    @Bean
    public HelmRepoService defaultHelmRepoService() {
        return new HelmRepoService();
    }

    @Bean
    public HelmInstallService defaultHelmInstallService() {
        return new HelmInstallService();
    }

    @Autowired
    private SecurityConfig securityConfig = new SecurityConfig();


    public HelmConfiguration getConfiguration(Region region, User user) {
        HelmConfiguration helmConfiguration = new HelmConfiguration();
        if (region.getServices().getServer() != null) {
            Region.Auth auth = region.getServices().getServer().getAuth();
            if (auth != null) {
                helmConfiguration.setKubeToken(auth.getToken());
            }
            helmConfiguration.setApiserverUrl(region.getServices().getServer().getUrl());
            helmConfiguration.setKubeConfig(null);
        }
        String username = user.getIdep();
        if (region.getServices().getUsernamePrefix() != null) {
            username = region.getServices().getUsernamePrefix()+username;
        }

        if (securityConfig.isStrictmode()) {
            helmConfiguration.setAsKubeUser(username);
        }

        return helmConfiguration;
    }
}
