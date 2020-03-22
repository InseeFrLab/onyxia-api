package fr.insee.onyxia.model.mesos;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class MesosContainerId {
	String value;

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
