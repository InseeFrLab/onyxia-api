package fr.insee.onyxia.model.mesos;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class MesosTasks {
	List<MesosTask> tasks;
	

	public List<MesosTask> getTasks() {
		return tasks;
	}

	public void setTasks(List<MesosTask> tasks) {
		this.tasks = tasks;
	}
}
