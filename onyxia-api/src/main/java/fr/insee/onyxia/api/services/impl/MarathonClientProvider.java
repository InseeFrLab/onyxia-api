package fr.insee.onyxia.api.services.impl;

import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.model.region.Region;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MarathonClientProvider {

    @Autowired
    private RegionsConfiguration regionsConfiguration;

    public Marathon getMarathonClientForRegion(Region region) {
        String MARATHON_URL = region.getServices().getServer().getUrl();
        String MARATHON_AUTH_TOKEN = null;
        String MARATHON_AUTH_BASIC_USERNAME = null;
        String MARATHON_AUTH_BASIC_PASSWORD = null;
        Region.Auth auth = region.getServices().getServer().getAuth();
        if (auth != null) {
            if (!StringUtils.isBlank(auth.getToken())) {
                MARATHON_AUTH_TOKEN =auth.getToken();
            }
            else if (!StringUtils.isBlank(auth.getUsername()) && !StringUtils.isBlank(auth.getPassword())) {
                MARATHON_AUTH_BASIC_USERNAME =auth.getUsername();
                MARATHON_AUTH_BASIC_PASSWORD =auth.getPassword();
            }
        }
        if (MARATHON_URL == null || MARATHON_URL.isBlank()) {
            return null;
        }
        if (MARATHON_AUTH_TOKEN != null && !MARATHON_AUTH_TOKEN.isBlank()) {
            return MarathonClient.getInstanceWithTokenAuth(MARATHON_URL,MARATHON_AUTH_TOKEN);
        }
        else if (MARATHON_AUTH_BASIC_USERNAME != null && !MARATHON_AUTH_BASIC_USERNAME.isBlank()
                && MARATHON_AUTH_BASIC_PASSWORD != null && !MARATHON_AUTH_BASIC_PASSWORD.isBlank()) {
            return MarathonClient.getInstanceWithBasicAuth(MARATHON_URL,MARATHON_AUTH_BASIC_USERNAME, MARATHON_AUTH_BASIC_PASSWORD);
        }
        else {
            return MarathonClient.getInstance(MARATHON_URL);
        }
    }
}
