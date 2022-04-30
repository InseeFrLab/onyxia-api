package fr.insee.onyxia.model.region;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Server {

	private String publicUrl;
	private String privateUrl;
	private Auth auth;
	private KeycloakParams keycloakParams;

	public String getPublicUrl() {
	    return publicUrl;
	}

	public void setPublicUrl(String publicUrl) {
	    this.publicUrl = publicUrl;
	}

	@JsonIgnore
	public String getPrivateUrl() {
	    return privateUrl;
	}

	public void setPrivateUrl(String privateUrl) {
	    this.privateUrl = privateUrl;
	}

	@JsonIgnore
	public Auth getAuth() {
	    return auth;
	}

	public void setAuth(Auth auth) {
	    this.auth = auth;
	}

	public void setKeycloakParams(KeycloakParams keycloakParams) {
	    this.keycloakParams = keycloakParams;
	}

	public KeycloakParams getKeycloakParams() {
	    return keycloakParams;
	}

    public static class Auth {
	    private String token;
	    private String username;
        private String password;

	    public String getToken() {
	        return token;
	    }

	    public void setToken(String token) {
	        this.token = token;
	    }

	    public String getUsername() {
	        return username;
	    }

	    public void setUsername(String username) {
	        this.username = username;
	    }

	    public String getPassword() {
	        return password;
	    }

	    public void setPassword(String password) {
	        this.password = password;
	    }
    }
}
