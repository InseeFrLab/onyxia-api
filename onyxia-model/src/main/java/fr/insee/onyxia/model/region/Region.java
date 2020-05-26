package fr.insee.onyxia.model.region;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.insee.onyxia.model.service.Service;

@JsonIgnoreProperties(value={"auth","serverUrl"},allowSetters = true)
public class Region {
    String id;
    String name;
    Service.ServiceType type;
    String serverUrl;
    String publishDomain;
    String namespacePrefix;
    String marathonDnsSuffix;
    Auth auth;
    @JsonProperty("cloudshell")
    CloudshellConfiguration cloudshellConfiguration;
    @JsonProperty("serviceMonitoringURLPattern")
    String serviceMonitoringURLPattern;
    Location location;

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

    public Service.ServiceType getType() {
        return type;
    }

    public void setType(Service.ServiceType type) {
        this.type = type;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getPublishDomain() {
        return publishDomain;
    }

    public void setPublishDomain(String publishDomain) {
        this.publishDomain = publishDomain;
    }

    public String getNamespacePrefix() {
        return namespacePrefix;
    }

    public void setNamespacePrefix(String namespacePrefix) {
        this.namespacePrefix = namespacePrefix;
    }

    public String getMarathonDnsSuffix() {
        return marathonDnsSuffix;
    }

    public void setMarathonDnsSuffix(String marathonDnsSuffix) {
        this.marathonDnsSuffix = marathonDnsSuffix;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public CloudshellConfiguration getCloudshellConfiguration() {
        return cloudshellConfiguration;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setCloudshellConfiguration(CloudshellConfiguration cloudshellConfiguration) {
        this.cloudshellConfiguration = cloudshellConfiguration;
    }

    public void setServiceMonitoringURLPattern(String serviceMonitoringURLPattern) {
        this.serviceMonitoringURLPattern = serviceMonitoringURLPattern;
    }

    public String getServiceMonitoringURLPattern() {
        return serviceMonitoringURLPattern;
    }

    public static class Location {

        private double lat;
        @JsonProperty("long")
        private double longitude;
        private String name;


        public void setLat(double lat) {
            this.lat = lat;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getLat() {
            return lat;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class CloudshellConfiguration {

        private String catalogId, packageName;

        public String getCatalogId() {
            return catalogId;
        }

        public void setCatalogId(String catalogId) {
            this.catalogId = catalogId;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }
    }

    public static class Auth {
        private String token;
        private String username, password;

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
