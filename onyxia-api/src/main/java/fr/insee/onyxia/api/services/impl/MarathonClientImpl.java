package fr.insee.onyxia.api.services.impl;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class MarathonClientImpl {

    @Value("${marathon.url}")
    private String MARATHON_URL;

    @Value("${marathon.auth.token}")
    private String MARATHON_AUTH_TOKEN;

    @Value("${marathon.auth.basic.username}")
    private String MARATHON_AUTH_BASIC_USERNAME;

    @Value("${marathon.auth.basic.password}")
    private String MARATHON_AUTH_BASIC_PASSWORD;

    @Bean
    public Marathon marathonClient() {
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
