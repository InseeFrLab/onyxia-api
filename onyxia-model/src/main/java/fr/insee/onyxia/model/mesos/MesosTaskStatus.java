package fr.insee.onyxia.model.mesos;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class MesosTaskStatus {
	String state;
	MesosContainerStatus container_status;
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public MesosContainerStatus getContainer_status() {
		return container_status;
	}
	public void setContainer_status(MesosContainerStatus container_status) {
		this.container_status = container_status;
	}

}
