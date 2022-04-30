package fr.insee.onyxia.api.configuration.kubernetes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.insee.onyxia.api.configuration.SecurityConfig;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.region.Region;
import fr.insee.onyxia.model.region.Server;
import io.github.inseefrlab.helmwrapper.configuration.HelmConfiguration;
import io.github.inseefrlab.helmwrapper.service.HelmInstallService;
import io.github.inseefrlab.helmwrapper.service.HelmRepoService;

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
    private final SecurityConfig securityConfig = new SecurityConfig();


    public HelmConfiguration getConfiguration(Region region, User user) {
	final HelmConfiguration helmConfiguration = new HelmConfiguration();
	if (region.getServices().getServer() != null) {
	    final Server.Auth auth = region.getServices().getServer().getAuth();
	    if (auth != null) {
		helmConfiguration.setKubeToken(auth.getToken());
	    }
	    helmConfiguration.setApiserverUrl(region.getServices().getServer().getPrivateUrl());
	    helmConfiguration.setKubeConfig(null);
	}
	String username = user.getIdep();
	if (region.getServices().getUsernamePrefix() != null) {
	    username = region.getServices().getUsernamePrefix()+username;
	}

	if (region.getServices().getAuthenticationMode() == Region.Services.AuthenticationMode.IMPERSONATE) {
	    helmConfiguration.setAsKubeUser(username);
	}

	if (region.getServices().getAuthenticationMode() == Region.Services.AuthenticationMode.USER) {
	    helmConfiguration.setKubeToken((String) user.getAttributes().get("access_token"));
	}

	return helmConfiguration;
    }
}
