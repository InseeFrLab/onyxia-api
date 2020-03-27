package fr.insee.onyxia.model.catalog.Config;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

/**
 * Properties
 */
public class Category {

	Map<String, Property> properties = new HashMap<>();
	String description;
	String type;
	String[] required;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@JsonAnyGetter
	public Map<String, Property> getProperties() {
		// Map<String,Property> map = new HashMap<>();
		// for (Entry<String,Property> entry : properties.entrySet()) {
		// //if (entry.getValue().getApiDefined() == null) {
		// map.put(entry.getKey(), entry.getValue());
		// }
		// }
		return properties;
	}

	// @Transient
	// public Map<String,Property> getAllProperties() {
	// return properties;
	// }
	public String[] getRequired() {
		return required;
	}

	public void setRequired(String[] required) {
		this.required = required;
	}

	@JsonAnySetter
	public void setUnrecognizedFields(String key, Property value) {
		this.properties.put(key, value);
	}

	public void setProperties(Map<String, Property> properties) {
		this.properties = properties;
	}

}