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
import java.io.InputStream;

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
    public Multiverse multiverse() {
            try(InputStream inputStream = resourceLoader.getResource(multiverseConf).getInputStream()){
                return mapper.readValue(inputStream, Multiverse.class);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
    }


}
