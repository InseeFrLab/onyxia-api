package fr.insee.onyxia.model.catalog.Config;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Property {
    String type;
    String description;
    String title;
    @JsonProperty("default")
    Object defaut;
    Media media;
    String minimum;
    @JsonProperty("enum")
    Object enumeration;
    Map<String, Property> properties;

    @JsonProperty("api-defined")
    Boolean apiDefined = null;

    @JsonProperty("api-default")
    String apiDefault;

    @JsonProperty("js-control")
    String jsControl;

    @JsonProperty("api-control")
    String apiControl;

    public Boolean getApiDefined() {
        return apiDefined;
    }

    public void setApiDefined(Boolean apiDefined) {
        this.apiDefined = apiDefined;
    }

    public String getJsControl() {
        return jsControl;
    }

    public void setJsControl(String jsControl) {
        this.jsControl = jsControl;
    }

    public String getApiDefault() {
        return apiDefault;
    }

    public void setApiDefault(String apiDefault) {
        this.apiDefault = apiDefault;
    }

    public String getApiControl() {
        return apiControl;
    }

    public void setApiControl(String apiControl) {
        this.apiControl = apiControl;
    }

    public Map<String, Property> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Property> properties) {
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public Object getDefaut() {
        return defaut;
    }

    public void setDefaut(Object defaut) {
        this.defaut = defaut;
    }

    public Object getEnumeration() {
        return enumeration;
    }

    public void setEnumeration(Object enumeration) {
        this.enumeration = enumeration;
    }

    public String getMinimum() {
        return minimum;
    }

    public void setMinimum(String minimum) {
        this.minimum = minimum;
    }

    public boolean enforceUser(Map<String, String> userValues, Map<String, Object> map, String key) {
        switch (type) {
            case "string":
                if (apiDefined != null && apiControl != null && apiDefined) {
                    switch (apiControl) {
                        case "strict":
                            String force = apiDefault;
                            for (Map.Entry<String, String> entry : userValues.entrySet()) {
                                if (entry.getValue() != null) {
                                    force = force.replaceAll(entry.getKey(), entry.getValue());
                                }
                            }
                            map.put(key, force);
                            break;

                    }
                }
                break;
            case "boolean":
                break;
            case "number":
                break;
            case "object":
                if (!(map.get(key) instanceof Map)) {
                    return false;
                }
                for (Map.Entry<String, Property> entry : properties.entrySet()) {
                    entry.getValue().enforceUser(userValues, (Map<String, Object>) map.get(key), entry.getKey());
                }

                break;
            default:
                return false;

        }
        return true;
    }
    // public void initUser(User user) {
    // if (properties!=null){
    // for (Property property : properties.values()){
    // property.initUser(user);
    // }
    // }
    // else{
    // defaut = apiDefault;
    // }

    // }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Media {
        String type;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

}
