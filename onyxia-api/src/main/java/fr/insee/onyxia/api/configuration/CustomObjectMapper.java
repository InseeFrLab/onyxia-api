package fr.insee.onyxia.api.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Configuration
public class CustomObjectMapper {

   @Bean
   public ObjectMapper objectMapper() {
       ObjectMapper mapper = new ObjectMapper();
       mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
       mapper.setSerializationInclusion(Include.NON_NULL);
       mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
       mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
       return mapper;
   }
}
