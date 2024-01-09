package fr.insee.onyxia.api.configuration;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CustomObjectMapper {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        JsonMapper.Builder mapper = JsonMapper.builder();
        commonConfiguration(mapper);
        return mapper.build();
    }

    @Bean
    @Qualifier("helm")
    public ObjectMapper objectMapperHelm() {
        JsonMapper.Builder mapper = JsonMapper.builder(new YAMLFactory());
        commonConfiguration(mapper);
        return mapper.build();
    }

    private void commonConfiguration(JsonMapper.Builder mapper) {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.serializationInclusion(Include.NON_NULL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    }
}
