package fr.insee.onyxia.api;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"io.github.inseefrlab", "fr.insee.onyxia"})
@EnableAsync(proxyTargetClass = true)
public class Application {

    // logger
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                new SpringApplicationBuilder(Application.class)
                        .listeners(new DynamicPropertiesListener())
                        .run(args);
    }

    public static class DynamicPropertiesListener
            implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
        public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
            // Use alias for properties
            ConfigurableEnvironment environment = event.getEnvironment();
            MutablePropertySources propertySources = environment.getPropertySources();
            Map<String, Object> myMap = new HashMap<>();
            String oidcJwkUri = environment.getProperty("oidc.jwk-uri");
            String oidcIssuerUri = environment.getProperty("oidc.issuer-uri");
            LOGGER.info("oidc properties, jwk-uri: {}, issuer-uri: {}", oidcJwkUri, oidcIssuerUri);

            if (StringUtils.isNotEmpty(oidcIssuerUri)) {
                myMap.put("spring.security.oauth2.resourceserver.jwt.issuer-uri", oidcIssuerUri);
            }
            if (StringUtils.isNotEmpty(oidcJwkUri)) {
                myMap.put("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", oidcJwkUri);
            }
            propertySources.addFirst(new MapPropertySource("ALIASES", myMap));
        }
    }
}
