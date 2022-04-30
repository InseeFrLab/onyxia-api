package fr.insee.onyxia.model.region;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KeycloakParams {
    
	@JsonProperty("URL")
	private String url;
	private String clientId;
	private String realm;

	public String getUrl() {
	    return url;
	}

	public void setUrl(String url) {
	    this.url = url;
	}

	public String getClientId() {
	    return clientId;
	}

	public void setUClientId(String clientId) {
	    this.clientId = clientId;
	}

	public String getRealm() {
	    return realm;
	}

	public void setRealm(String realm) {
	    this.realm = realm;
	}
}
