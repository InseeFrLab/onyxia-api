package fr.insee.onyxia.api.configuration;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import mesosphere.marathon.client.model.v2.ExternalVolume;
import mesosphere.marathon.client.model.v2.LocalVolume;
import mesosphere.marathon.client.model.v2.PersistentLocalVolume;
import mesosphere.marathon.client.model.v2.Volume;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;

@Configuration
public class CustomObjectMapper {

   @Bean
   @Primary
   public ObjectMapper objectMapper() {
       ObjectMapper mapper = new ObjectMapper();
       commonConfiguration(mapper);
       SimpleModule module = new SimpleModule();
       module.addDeserializer(Volume.class, new ItemDeserializer(mapper));
       mapper.registerModule(module);

       return mapper;
   }

    @Bean
    @Qualifier("helm")
    public ObjectMapper objectMapperHelm() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        commonConfiguration(mapper);

        return mapper;
    }

    private void commonConfiguration(ObjectMapper mapper) {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    }

    public class ItemDeserializer extends StdDeserializer<Volume> {

       ObjectMapper mapper;

        public ItemDeserializer(ObjectMapper mapper) {
            this(mapper,null);
        }

        public ItemDeserializer(ObjectMapper mapper, Class<?> vc) {
            super(vc);
            this.mapper = mapper;
        }

        @Override
        public Volume deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            JsonNode node = jp.getCodec().readTree(jp);
            if (node != null && node.has("external")) {
                return mapper.treeToValue(node, ExternalVolume.class);
            }
            if (node != null && node.has("persistent")) {
                return mapper.treeToValue(node, PersistentLocalVolume.class);
            }
            return mapper.treeToValue(node, LocalVolume.class);
        }
    }
}
