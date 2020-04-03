package fr.insee.onyxia.model.catalog;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import fr.insee.onyxia.model.catalog.Config.Config;
import fr.insee.onyxia.model.catalog.Package;

public class UniversePackage extends Package {

	String packagingVersion;
	String name;
	String version;
	String minDcosReleaseVersion;
	String scm;
	String maintainer;
	String website;
	boolean framework;
	String description;
	List<String> tags;
	String preInstallNotes;
	String postInstallNotes;
	String postUninstallNotes;
	List<License> licenses;
	boolean selected;
	int lastUpdated;
	int releaseVersion;
	Map<String, Object> resource = new HashMap<>();
	MarathonMustache marathon;
	Config config;
	String jsonMustache;

	String status;
	boolean disable;
	// String category;

	@JsonIgnore
	public String getJsonMustache() {
		if (jsonMustache == null && marathon != null && marathon.getV2AppMustacheTemplate() != null) {
			try {
				jsonMustache = new String(Base64.getDecoder().decode(marathon.getV2AppMustacheTemplate()));
			} catch (IllegalArgumentException e) {
				// donothing
			}
		}
		return jsonMustache;
	}

	public static class MarathonMustache {

		String v2AppMustacheTemplate;

		public String getV2AppMustacheTemplate() {
			return v2AppMustacheTemplate;
		}

		public void setV2AppMustacheTemplate(String v2AppMustacheTemplate) {
			this.v2AppMustacheTemplate = v2AppMustacheTemplate;
		}

	}

	public class License {
		String name;
		String url;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

	}

	public MarathonMustache getMarathon() {
		return marathon;
	}

	public void setMarathon(MarathonMustache marathon) {
		this.marathon = marathon;
	}

	public boolean isDisable() {
		return disable;
	}

	public void setDisable(boolean disable) {
		this.disable = disable;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getReleaseVersion() {
		return releaseVersion;
	}

	public void setReleaseVersion(int releaseVersion) {
		this.releaseVersion = releaseVersion;
	}

	public int getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(int lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public String getMinDcosReleaseVersion() {
		return minDcosReleaseVersion;
	}

	public void setMinDcosReleaseVersion(String minDcosReleaseVersion) {
		this.minDcosReleaseVersion = minDcosReleaseVersion;
	}

	public String getPostUninstallNotes() {
		return postUninstallNotes;
	}

	public void setPostUninstallNotes(String postUninstallNotes) {
		this.postUninstallNotes = postUninstallNotes;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getScm() {
		return scm;
	}

	public void setScm(String scm) {
		this.scm = scm;
	}

	public String getMaintainer() {
		return maintainer;
	}

	public void setMaintainer(String maintainer) {
		this.maintainer = maintainer;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getPreInstallNotes() {
		return preInstallNotes;
	}

	public void setPreInstallNotes(String preInstallNotes) {
		this.preInstallNotes = preInstallNotes;
	}

	public String getPostInstallNotes() {
		return postInstallNotes;
	}

	public void setPostInstallNotes(String postInstallNotes) {
		this.postInstallNotes = postInstallNotes;
	}

	public boolean isFramework() {
		return framework;
	}

	public void setFramework(boolean framework) {
		this.framework = framework;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getPackagingVersion() {
		return packagingVersion;
	}

	public void setPackagingVersion(String packagingVersion) {
		this.packagingVersion = packagingVersion;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public Map<String, Object> getResource() {
		return resource;
	}

	public void setResource(Map<String, Object> resource) {
		this.resource = resource;
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config properties) {
		this.config = properties;
	}

	public static class Images {
		@JsonProperty("icon-large")
		String iconLarge;
		@JsonProperty("icon-small")
		String iconSmall;
		@JsonProperty("icon-medium")
		String iconMedium;

	}

}
