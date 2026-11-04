package fr.insee.onyxia.api.configuration;

import static tools.jackson.core.json.JsonReadFeature.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import fr.insee.onyxia.model.views.Views;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

@Configuration
public class CustomObjectMapper {

    /**
     * Main mapper, also used by Spring MVC for HTTP messages. Declared as a {@link JsonMapper} so
     * that Spring Boot's Jackson auto-configuration backs off and uses it.
     */
    @Bean
    @Primary
    public JsonMapper objectMapper() {
        JsonMapper.Builder mapper =
                JsonMapper.builder()
                        .enable(
                                ALLOW_JAVA_COMMENTS,
                                ALLOW_YAML_COMMENTS,
                                ALLOW_TRAILING_COMMA,
                                ALLOW_SINGLE_QUOTES)
                        .defaultSerializationView(Views.General.class);
        commonConfiguration(mapper);
        return mapper.build();
    }

    @Bean(name = "helm")
    public YAMLMapper objectMapperHelm() {
        YAMLMapper.Builder mapper = YAMLMapper.builder();
        commonConfiguration(mapper);
        return mapper.build();
    }

    private static void commonConfiguration(MapperBuilder<?, ?> mapper) {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .changeDefaultPropertyInclusion(
                        incl -> JsonInclude.Value.construct(Include.NON_NULL, Include.NON_NULL));
        keepJackson2Defaults(mapper);
    }

    /**
     * Jackson 3 changed several defaults. Restore the Jackson 2 ones so that the API contract (JSON
     * returned to clients, accepted input, Helm values) does not change.
     */
    private static void keepJackson2Defaults(MapperBuilder<?, ?> mapper) {
        mapper
                // A default serialization view is set: without this, every property lacking
                // @JsonView would disappear from responses.
                .enable(MapperFeature.DEFAULT_VIEW_INCLUSION)
                // Keep declaration order of properties
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .enable(MapperFeature.USE_GETTERS_AS_SETTERS)
                .enable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)
                .disable(MapperFeature.FIX_FIELD_NAME_UPPER_CASE_PREFIX)
                .disable(MapperFeature.DETECT_PARAMETER_NAMES)
                // Stay lenient on input
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                // Enums are (de)serialized using name(), not toString()
                .disable(EnumFeature.READ_ENUMS_USING_TO_STRING)
                .disable(EnumFeature.WRITE_ENUMS_USING_TO_STRING);
    }
}
