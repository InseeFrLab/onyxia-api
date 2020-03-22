package fr.insee.onyxia.model.mesos;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class MesosSlaves {
	List<MesosSlave> slaves;

	public List<MesosSlave> getSlaves() {
		return slaves;
	}

	public void setSlaves(List<MesosSlave> slaves) {
		this.slaves = slaves;
	}
}
