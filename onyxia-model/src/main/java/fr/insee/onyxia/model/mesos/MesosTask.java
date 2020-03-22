package fr.insee.onyxia.model.mesos;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class MesosTask {
	
	String id;
	String name;
	String framework_id;
	String executor_id;
	String slave_id;
	String state;
	List<MesosTaskStatus> statuses;	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFramework_id() {
		return framework_id;
	}
	public void setFramework_id(String framework_id) {
		this.framework_id = framework_id;
	}
	public String getExecutor_id() {
		return executor_id;
	}
	public void setExecutor_id(String executor_id) {
		this.executor_id = executor_id;
	}
	public String getSlave_id() {
		return slave_id;
	}
	public void setSlave_id(String slave_id) {
		this.slave_id = slave_id;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public List<MesosTaskStatus> getStatuses() {
		return statuses;
	}
	public void setStatuses(List<MesosTaskStatus> statuses) {
		this.statuses = statuses;
	}

}
