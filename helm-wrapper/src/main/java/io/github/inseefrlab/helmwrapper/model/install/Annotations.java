
package io.github.inseefrlab.helmwrapper.model.install;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "kubernetes.io/ingress.class" })
public class Annotations {

    @JsonProperty("kubernetes.io/ingress.class")
    private String kubernetesIoIngressClass;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("kubernetes.io/ingress.class")
    public String getKubernetesIoIngressClass() {
        return kubernetesIoIngressClass;
    }

    @JsonProperty("kubernetes.io/ingress.class")
    public void setKubernetesIoIngressClass(String kubernetesIoIngressClass) {
        this.kubernetesIoIngressClass = kubernetesIoIngressClass;
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
