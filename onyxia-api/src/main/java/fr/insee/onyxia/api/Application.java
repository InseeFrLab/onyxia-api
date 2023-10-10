package fr.insee.onyxia.api;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
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
            System.out.println(environment.getProperty("oidc.jwk-uri"));
            if (StringUtils.isNotEmpty(environment.getProperty("oidc.issuer-uri"))) {
                myMap.put(
                        "spring.security.oauth2.resourceserver.jwt.issuer-uri",
                        environment.getProperty("oidc.issuer-uri"));
            }
            if (StringUtils.isNotEmpty(environment.getProperty("oidc.jwk-uri"))) {
                myMap.put(
                        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                        environment.getProperty("oidc.jwk-uri"));
            }
            propertySources.addFirst(new MapPropertySource("ALIASES", myMap));
        }
    }
}
