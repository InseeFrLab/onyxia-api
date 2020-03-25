package fr.insee.onyxia.model.catalog.Config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Config
 */
public class Config {

    private String type;
    private Category properties;
    private List<String> required = null;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Category getProperties() {
        return properties;
    }

    public void setProperties(Category properties) {
        this.properties = properties;
    }

    public List<String> getRequired() {
        return required;
    }

    public void setRequired(List<String> required) {
        this.required = required;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public boolean enforceUser(Map<String, String> userValues, Object object) {
         Map<String, Object> map;
         if (!(object instanceof Map)) {
         return false;
         } else {
         map = (Map<String, Object>) object;
         }
         for (Map.Entry<String, Property> entry : properties.getProperties().entrySet()) {
         entry.getValue().enforceUser(userValues, map, entry.getKey());
         }
        return true;
    }

}