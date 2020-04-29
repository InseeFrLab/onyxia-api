package fr.insee.onyxia.api.configuration.kubernetes;

import fr.insee.onyxia.model.region.Region;
import io.github.inseefrlab.helmwrapper.configuration.HelmConfiguration;
import io.github.inseefrlab.helmwrapper.service.HelmInstallService;
import io.github.inseefrlab.helmwrapper.service.HelmRepoService;
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

    public HelmInstallService getHelmInstallServiceForRegion(Region region) {
        return new HelmInstallService(generateConfigurationForRegion(region));
    }


    private HelmConfiguration generateConfigurationForRegion(Region region) {
        HelmConfiguration helmConfiguration = new HelmConfiguration();
        if (region.getAuth() != null) {
            helmConfiguration.setKubeToken(region.getAuth().getToken());
        }
        helmConfiguration.setApiserverUrl(region.getServerUrl());
        helmConfiguration.setKubeConfig(null);
        return helmConfiguration;
    }
}
