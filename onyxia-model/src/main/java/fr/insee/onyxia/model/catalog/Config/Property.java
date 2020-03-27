package fr.insee.onyxia.model.catalog.Config;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.print.attribute.standard.Media;

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

    @JsonProperty("x-form")
    private XForm xform;

    public XForm getXform() {
        return xform;
    }

    public void setXform(XForm xform) {
        this.xform = xform;
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class XForm {

        boolean visible = true;
        boolean readonly = false;
        String value;

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public boolean isReadonly() {
            return readonly;
        }

        public void setReadonly(boolean readonly) {
            this.readonly = readonly;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

}
