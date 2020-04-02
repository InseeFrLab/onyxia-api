package fr.insee.onyxia.api.configuration.helm;

import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class HelmRepoLoader {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ObjectMapper mapper;

    @Value("${helmRepo.configuration}")
    private String helmRepoConf;

    Logger logger = LoggerFactory.getLogger(HelmRepoLoader.class);

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public HelmRepo helmRepoLoader() {
        try (InputStream inputStream = resourceLoader.getResource(helmRepoConf).getInputStream()) {
            return mapper.readValue(inputStream, HelmRepo.class);
        } catch (Exception e) {
            logger.error("Error : Could not load multiverse !", e);
        }
        return new HelmRepo();
    }

}