package fr.insee.onyxia.model.region;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.insee.onyxia.model.service.Service;

public class Region {
    private String id;
    private String name;
    private String description;
    private Location location;
    private Services services;
    private OnyxiaAPI onyxiaAPI;
    private Data data;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Services getServices() {
        return services;
    }

    public void setServices(Services services) {
        this.services = services;
    }


    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public OnyxiaAPI getOnyxiaAPI() {
        return onyxiaAPI;
    }

    public void setOnyxiaAPI(OnyxiaAPI onyxiaAPI) {
        this.onyxiaAPI = onyxiaAPI;
    }

    @JsonIgnoreProperties(value={"server"},allowSetters = true)
    public static class Services {

        private Service.ServiceType type;
        private String network;
        private String namespacePrefix;
        private String marathonDnsSuffix;
        private Expose expose;
        private Server server;
        private Monitoring monitoring;
        private CloudshellConfiguration cloudshell;

        public Service.ServiceType getType() {
            return type;
        }

        public void setType(Service.ServiceType type) {
            this.type = type;
        }

        public String getNetwork() {
            return network;
        }

        public void setNetwork(String network){
            this.network = network;
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

        public Expose getExpose() {
            return expose;
        }

        public void setExpose(Expose expose) {
            this.expose = expose;
        }

        public Server getServer() {
            return server;
        }

        public void setServer(Server server) {
            this.server = server;
        }

        public CloudshellConfiguration getCloudshell() {
            return cloudshell;
        }

        public void setCloudshell(CloudshellConfiguration cloudshell) {
            this.cloudshell = cloudshell;
        }

        public Monitoring getMonitoring() {
            return monitoring;
        }

        public void setMonitoring(Monitoring monitoring) {
            this.monitoring = monitoring;
        }
    }

    public static class Monitoring {
        @JsonProperty("URLPattern")
        private String urlPattern;

        public void setUrlPattern(String urlPattern) {
            this.urlPattern = urlPattern;
        }

        public String getUrlPattern() {
            return urlPattern;
        }
    }

    public static class Data {
        @JsonProperty("S3")
        private S3 s3;

        public S3 getS3() {
            return s3;
        }

        public void setS3(S3 s3) {
            this.s3 = s3;
        }
    }

    public static class S3 {
        @JsonProperty("URL")
        private String url;

        private Monitoring monitoring;

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public void setMonitoring(Monitoring monitoring) {
            this.monitoring = monitoring;
        }

        public Monitoring getMonitoring() {
            return monitoring;
        }
    }

    public static class Expose {
        private String domain;

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getDomain() {
            return domain;
        }
    }

    public static class Server {

        @JsonProperty("URL")
        private String url;
        private Auth auth;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Auth getAuth() {
            return auth;
        }

        public void setAuth(Auth auth) {
            this.auth = auth;
        }
    }

    public static class OnyxiaAPI {
        private String baseURL;

        public String getBaseURL() {
            return baseURL;
        }

        public void setBaseURL(String baseURL) {
            this.baseURL = baseURL;
        }
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
