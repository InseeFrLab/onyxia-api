package fr.insee.onyxia.model.catalog.Config;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Cloudshell data and health")
public class Property {
    @Schema(description = "")
    String type;
    @Schema(description = "")
    String description;
    @Schema(description = "")
    String title;
    
    @Schema(description = "")
    @JsonProperty("default")
    Object defaut;
    @Schema(description = "")
    String pattern;
    @Schema(description = "")
    Media media;
    @Schema(description = "")
    String minimum;
    @Schema(description = "")
    String render;
    @Schema(description = "")
    Integer sliderMin;
    @Schema(description = "")
    Integer sliderMax;
    @Schema(description = "")
    Integer sliderStep;
    @Schema(description = "")
    String sliderUnit;
    @Schema(description = "")
    String sliderExtremity;
    @Schema(description = "")
    String sliderExtremitySemantic;
    @Schema(description = "")
    String sliderRangeId;
    @Schema(description = "")
    Hidden hidden;

    @JsonProperty("enum")
    @Schema(description = "")
    Object enumeration;
    @Schema(description = "")
    Map<String, Property> properties;

    @JsonProperty("x-form")
    @Schema(description = "")
    private XForm xform;


    @JsonProperty("x-security")
    @Schema(description = "")
    private XSecurity xsecurity;

    @JsonProperty("x-generated")
    @Schema(description = "")
    private XGenerated xGenerated;

    @JsonProperty("x-onyxia")
    @Schema(description = "")
    private XOnyxia xonyxia;

    public XForm getXform() {
        return xform;
    }

    public void setXform(XForm xform) {
        this.xform = xform;
    }

    public XOnyxia getXonyxia() {
        return xonyxia;
    }

    public void setXonyxia(XOnyxia xonyxia) {
        this.xonyxia = xonyxia;
    }

    public XSecurity getXsecurity() {
        return xsecurity;
    }

    public void setXsecurity(XSecurity xsecurity) {
        this.xsecurity = xsecurity;
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

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getRender() {
        return render;
    }

    public void setRender(String render) {
        this.render = render;
    }

    public Integer getSliderMin() {
        return sliderMin;
    }

    public void setSliderMin(Integer sliderMin) {
        this.sliderMin = sliderMin;
    }

    public Integer getSliderMax() {
        return sliderMax;
    }

    public void setSliderMax(Integer sliderMax) {
        this.sliderMax = sliderMax;
    }


    public Integer getSliderStep() {
        return sliderStep;
    }

    public void setSliderStep(Integer sliderStep) {
        this.sliderStep = sliderStep;
    }


    public String getSliderUnit() {
        return sliderUnit;
    }

    public void setSliderUnit(String sliderUnit) {
        this.sliderUnit = sliderUnit;
    }

    public String getSliderExtremity() {
        return sliderExtremity;
    }

    public void setSliderExtremity(String sliderExtremity) {
        this.sliderExtremity = sliderExtremity;
    }

    public String getSliderExtremitySemantic() {
        return sliderExtremitySemantic;
    }

    public void setSliderExtremitySemantic(String sliderExtremitySemantic) {
        this.sliderExtremitySemantic = sliderExtremitySemantic;
    }

    public String getSliderRangeId() {
        return sliderRangeId;
    }

    public void setSliderRangeId(String sliderRangeId) {
        this.sliderRangeId = sliderRangeId;
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
    @Schema(description = "Cloudshell data and health")
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
    @Schema(description = "Cloudshell data and health")
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
    public static class XSecurity {

        String pattern;

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
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
    public static class XOnyxia {

        boolean hidden = false;
        boolean readonly = false;
        String overwriteDefaultWith;

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

        public String getOverwriteDefaultWith() {
            return overwriteDefaultWith;
        }

        public void setOverwriteDefaultWith(String overwriteDefaultWith) {
            this.overwriteDefaultWith = overwriteDefaultWith;
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
