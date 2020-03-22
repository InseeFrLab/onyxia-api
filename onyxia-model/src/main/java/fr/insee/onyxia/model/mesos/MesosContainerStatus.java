package fr.insee.onyxia.model.mesos;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class MesosContainerStatus {
	MesosContainerId container_id;

	public MesosContainerId getContainer_id() {
		return container_id;
	}

	public void setContainer_id(MesosContainerId container_id) {
		this.container_id = container_id;
	}
}
