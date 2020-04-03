
package io.github.inseefrlab.helmwrapper.model.install;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "affinity", "fullnameOverride", "image", "imagePullSecrets", "ingress", "nameOverride",
        "nodeSelector", "podSecurityContext", "replicaCount", "resources", "securityContext", "service",
        "tolerations" })
public class Values {

    @JsonProperty("affinity")
    private Affinity affinity;
    @JsonProperty("fullnameOverride")
    private String fullnameOverride;
    @JsonProperty("image")
    private Image image;
    @JsonProperty("imagePullSecrets")
    private List<Object> imagePullSecrets = null;
    @JsonProperty("ingress")
    private Ingress ingress;
    @JsonProperty("nameOverride")
    private String nameOverride;
    @JsonProperty("nodeSelector")
    private NodeSelector nodeSelector;
    @JsonProperty("podSecurityContext")
    private PodSecurityContext podSecurityContext;
    @JsonProperty("replicaCount")
    private Integer replicaCount;
    @JsonProperty("resources")
    private Resources resources;
    @JsonProperty("securityContext")
    private SecurityContext securityContext;
    @JsonProperty("service")
    private Service service;
    @JsonProperty("tolerations")
    private List<Object> tolerations = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("affinity")
    public Affinity getAffinity() {
        return affinity;
    }

    @JsonProperty("affinity")
    public void setAffinity(Affinity affinity) {
        this.affinity = affinity;
    }

    @JsonProperty("fullnameOverride")
    public String getFullnameOverride() {
        return fullnameOverride;
    }

    @JsonProperty("fullnameOverride")
    public void setFullnameOverride(String fullnameOverride) {
        this.fullnameOverride = fullnameOverride;
    }

    @JsonProperty("image")
    public Image getImage() {
        return image;
    }

    @JsonProperty("image")
    public void setImage(Image image) {
        this.image = image;
    }

    @JsonProperty("imagePullSecrets")
    public List<Object> getImagePullSecrets() {
        return imagePullSecrets;
    }

    @JsonProperty("imagePullSecrets")
    public void setImagePullSecrets(List<Object> imagePullSecrets) {
        this.imagePullSecrets = imagePullSecrets;
    }

    @JsonProperty("ingress")
    public Ingress getIngress() {
        return ingress;
    }

    @JsonProperty("ingress")
    public void setIngress(Ingress ingress) {
        this.ingress = ingress;
    }

    @JsonProperty("nameOverride")
    public String getNameOverride() {
        return nameOverride;
    }

    @JsonProperty("nameOverride")
    public void setNameOverride(String nameOverride) {
        this.nameOverride = nameOverride;
    }

    @JsonProperty("nodeSelector")
    public NodeSelector getNodeSelector() {
        return nodeSelector;
    }

    @JsonProperty("nodeSelector")
    public void setNodeSelector(NodeSelector nodeSelector) {
        this.nodeSelector = nodeSelector;
    }

    @JsonProperty("podSecurityContext")
    public PodSecurityContext getPodSecurityContext() {
        return podSecurityContext;
    }

    @JsonProperty("podSecurityContext")
    public void setPodSecurityContext(PodSecurityContext podSecurityContext) {
        this.podSecurityContext = podSecurityContext;
    }

    @JsonProperty("replicaCount")
    public Integer getReplicaCount() {
        return replicaCount;
    }

    @JsonProperty("replicaCount")
    public void setReplicaCount(Integer replicaCount) {
        this.replicaCount = replicaCount;
    }

    @JsonProperty("resources")
    public Resources getResources() {
        return resources;
    }

    @JsonProperty("resources")
    public void setResources(Resources resources) {
        this.resources = resources;
    }

    @JsonProperty("securityContext")
    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    @JsonProperty("securityContext")
    public void setSecurityContext(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    @JsonProperty("service")
    public Service getService() {
        return service;
    }

    @JsonProperty("service")
    public void setService(Service service) {
        this.service = service;
    }

    @JsonProperty("tolerations")
    public List<Object> getTolerations() {
        return tolerations;
    }

    @JsonProperty("tolerations")
    public void setTolerations(List<Object> tolerations) {
        this.tolerations = tolerations;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
