package fr.insee.onyxia.model.catalog.Config;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Property {
    String type;
    String description;
    String title;
    @JsonProperty("default")
    Object defaut;
    Media media;
    String minimum;
    String render;
    int sliderMin;
    int sliderMax;
    int sliderStep;
    String sliderUnit;
    SliderRef sliderRef;
    Hidden hidden;

    @JsonProperty("enum")
    Object enumeration;
    Map<String, Property> properties;

    @JsonProperty("x-form")
    private XForm xform;

    @JsonProperty("x-generated")
    private XGenerated xGenerated;

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

    public String getRender() {
        return render;
    }

    public void setRender(String render) {
        this.render = render;
    }

    public int getSliderMin() {
        return sliderMin;
    }

    public void setSliderMin(int sliderMin) {
        this.sliderMin = sliderMin;
    }

    public int getSliderMax() {
        return sliderMax;
    }

    public void setSliderMax(int sliderMax) {
        this.sliderMax = sliderMax;
    }


    public int getSliderStep() {
        return sliderStep;
    }

    public void setSliderStep(int sliderStep) {
        this.sliderStep = sliderStep;
    }


    public String getSliderUnit() {
        return sliderUnit;
    }

    public void setSliderUnit(String sliderUnit) {
        this.sliderUnit = sliderUnit;
    }

    public SliderRef getSliderRef() {
        return sliderRef;
    }

    public void setSliderRef(SliderRef sliderRef) {
        this.sliderRef = sliderRef;
    }

    public Hidden getHidden() {
        return hidden;
    }

    public void setHidden(Hidden hidden) {
        this.hidden = hidden;
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

    public XGenerated getxGenerated() {
        return xGenerated;
    }

    public void setxGenerated(XGenerated xGenerated) {
        this.xGenerated = xGenerated;
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
    public static class SliderRef {
        String sliderId;
        String sliderPos;

        public String getSliderId() {
            return sliderId;
        }

        public void setSliderId(String sliderId) {
            this.sliderId = sliderId;
        }

        public String getSliderPos() {
            return sliderPos;
        }

        public void setSliderPos(String sliderPos) {
            this.sliderPos = sliderPos;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Hidden {
        Object value;
        String path;

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class XForm {

        boolean hidden = false;
        boolean readonly = false;
        String value;

        public boolean isHidden() {
            return hidden;
        }

        public void setHidden(boolean hidden) {
            this.hidden = hidden;
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class XGenerated {

        private XGeneratedType type;
        private String scope;
        private String name;

        public XGeneratedType getType() {
            return type;
        }

        public void setType(XGeneratedType type) {
            this.type = type;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public static enum XGeneratedType {
            GroupID, AppID, RandomID, 
            
            @JsonProperty("containerNetworkName")
            ContainerNetworkName,
            @JsonProperty("internalDNS")
            @JsonAlias("internal-DNS")
            InternalDNS,
            @JsonProperty("externalDNS")
            @JsonAlias("external-DNS")
            ExternalDNS,
            @JsonProperty("initScript")
            InitScript;
        }
    }

}
