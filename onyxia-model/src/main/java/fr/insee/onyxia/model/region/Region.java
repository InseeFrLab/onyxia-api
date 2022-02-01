package fr.insee.onyxia.model.region;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.insee.onyxia.model.service.Service;
import fr.insee.onyxia.model.service.quota.Quota;

public class Region {
    private String id;
    private String name;
    private String description;
    private Location location;
    private Services services = new Services();
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

        public static enum AuthenticationMode {
            @JsonProperty("impersonate")
            IMPERSONATE,
            @JsonProperty("admin")
            ADMIN
        }

        private Service.ServiceType type;
        private boolean singleNamespace = true;
        private String namespacePrefix = "user-";
        private String groupNamespacePrefix = "projet-";
        private String usernamePrefix;
        private String groupPrefix;
        private AuthenticationMode authenticationMode = AuthenticationMode.IMPERSONATE;
        private Expose expose;
        private Server server;
        private Monitoring monitoring;
        private CloudshellConfiguration cloudshell;
        private String initScript;
        private Quotas quotas = new Quotas();
        private DefaultConfiguration defaultConfiguration = new DefaultConfiguration();

        public static class DefaultConfiguration {
            private boolean IPProtection = true;
            private boolean networkPolicy = true;

            public void setIPProtection(boolean IPProtection) {
                this.IPProtection = IPProtection;
            }

            public boolean isIPProtection() {
                return IPProtection;
            }

            public void setNetworkPolicy(boolean networkPolicy) {
                this.networkPolicy = networkPolicy;
            }

            public boolean isNetworkPolicy() {
                return networkPolicy;
            }
        }

        public static class Quotas {
            private boolean enabled = false;
            private boolean allowUserModification = true;
            @JsonProperty("default")
            private Quota defaultQuota;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public boolean isAllowUserModification() {
                return allowUserModification;
            }

            public void setAllowUserModification(boolean allowUserModification) {
                this.allowUserModification = allowUserModification;
            }

            public Quota getDefaultQuota() {
                return defaultQuota;
            }

            public void setDefaultQuota(Quota defaultQuota) {
                this.defaultQuota = defaultQuota;
            }
        }

        public DefaultConfiguration getDefaultConfiguration() {
            return defaultConfiguration;
        }

        public void setDefaultConfiguration(DefaultConfiguration defaultConfiguration) {
            this.defaultConfiguration = defaultConfiguration;
        }

        public boolean isSingleNamespace() {
            return singleNamespace;
        }

        public void setSingleNamespace(boolean singleNamespace) {
            this.singleNamespace = singleNamespace;
        }

        public Service.ServiceType getType() {
            return type;
        }

        public void setType(Service.ServiceType type) {
            this.type = type;
        }

        public String getNamespacePrefix() {
            return namespacePrefix;
        }

        public void setNamespacePrefix(String namespacePrefix) {
            this.namespacePrefix = namespacePrefix;
        }

        public String getUsernamePrefix() {
            return usernamePrefix;
        }

        public void setUsernamePrefix(String usernamePrefix) {
            this.usernamePrefix = usernamePrefix;
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

        public String getInitScript() {
            return initScript;
        }

        public void setInitScript(String initScript) {
            this.initScript = initScript;
        }

        public AuthenticationMode getAuthenticationMode() {
            return authenticationMode;
        }

        public void setAuthenticationMode(AuthenticationMode authenticationMode) {
            this.authenticationMode = authenticationMode;
        }

        public void setGroupNamespacePrefix(String groupNamespacePrefix) {
            this.groupNamespacePrefix = groupNamespacePrefix;
        }

        public String getGroupNamespacePrefix() {
            return groupNamespacePrefix;
        }

        public void setGroupPrefix(String groupPrefix) {
            this.groupPrefix = groupPrefix;
        }

        public String getGroupPrefix() {
            return groupPrefix;
        }

        public Quotas getQuotas() {
            return quotas;
        }

        public void setQuotas(Quotas quotas) {
            this.quotas = quotas;
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
        private String endpointS3;
        private String endpointSTS;
        private String region;
        private String roleARN;
        private String roleSessionName;
        private String bucketPrefix;
        private String groupBucketPrefix;
        private String bucketClaim = "preferred_username";

        private Monitoring monitoring;

        public String getEndpointS3() {
            return endpointS3;
        }

        public void setEndpointS3(String endpointS3) {
            this.endpointS3 = endpointS3;
        }

        public String getEndpointSTS() {
            return endpointSTS;
        }

        public void setEndpointSTS(String endpointSTS) {
            this.endpointSTS = endpointSTS;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getRoleARN() {
            return roleARN;
        }

        public void setRoleARN(String roleARN) {
            this.roleARN = roleARN;
        }

        public String getRoleSessionName() {
            return roleSessionName;
        }

        public void setRoleSessionName(String roleSessionName) {
            this.roleSessionName = roleSessionName;
        }

        public String getBucketPrefix() {
            return bucketPrefix;
        }

        public void setBucketPrefix(String bucketPrefix) {
            this.bucketPrefix = bucketPrefix;
        }

        public String getGroupBucketPrefix() {
            return groupBucketPrefix;
        }

        public void setGroupBucketPrefix(String groupBucketPrefix) {
            this.groupBucketPrefix = groupBucketPrefix;
        }

        public String getBucketClaim() {
            return bucketClaim;
        }

        public void setBucketClaim(String bucketClaim) {
            this.bucketClaim = bucketClaim;
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
