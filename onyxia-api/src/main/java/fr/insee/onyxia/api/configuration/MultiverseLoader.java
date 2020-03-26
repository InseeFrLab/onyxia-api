package fr.insee.onyxia.api.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

@Configuration
public class MultiverseLoader {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ObjectMapper mapper;

    @Value("${multiverse.configuration}")
    private String multiverseConf;

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public Multiverse multiverse() throws IOException {
        Multiverse multiverse = mapper.readValue(resourceLoader.getResource(multiverseConf).getInputStream(),
                Multiverse.class);
        return multiverse;
    }

}
