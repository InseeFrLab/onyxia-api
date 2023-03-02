package fr.insee.onyxia.model.region;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.insee.onyxia.model.service.Service;
import fr.insee.onyxia.model.service.quota.Quota;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "")
public class Region {
    @Schema(description = "")
    private String id;

    @Schema(description = "")
    private String name;

    @Schema(description = "")
    private String description;

    @Schema(description = "")
    private String includedGroupPattern;

    @Schema(description = "Indicate how to transform a group based on `includedGroupPattern`")
    private String transformGroupPattern;

    @Schema(description = "")
    private String excludedGroupPattern;

    @Schema(description = "")
    private Location location;

    @Schema(description = "")
    private Services services = new Services();

    @Schema(description = "")
    private OnyxiaAPI onyxiaAPI;

    @Schema(description = "")
    private Data data;

    @Schema(description = "")
    private Vault vault;

    @Schema(description = "")
    private ProxyInjection proxyInjection;

    @Schema(description = "")
    private PackageRepositoryInjection packageRepositoryInjection;

    @Schema(description = "")
    private CertificateAuthorityInjection certificateAuthorityInjection;

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

    public String getIncludedGroupPattern() {
        return includedGroupPattern;
    }

    public void setIncludedGroupPattern(String includedGroupPattern) {
        this.includedGroupPattern = includedGroupPattern;
    }

    public String getTransformGroupPattern() {
        return transformGroupPattern;
    }

    public void setTransformGroupPattern(String transformGroupPattern) {
        this.transformGroupPattern = transformGroupPattern;
    }

    public String getExcludedGroupPattern() {
        return excludedGroupPattern;
    }

    public void setExcludedGroupPattern(String excludedGroupPattern) {
        this.excludedGroupPattern = excludedGroupPattern;
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

    public Vault getVault() {
        return vault;
    }

    public ProxyInjection getProxyInjection() {
        return proxyInjection;
    }

    public void setProxyInjection(ProxyInjection proxyInjection) {
        this.proxyInjection = proxyInjection;
    }

    public PackageRepositoryInjection getPackageRepositoryInjection() {
        return packageRepositoryInjection;
    }

    public void setPackageRepositoryInjection(
            PackageRepositoryInjection packageRepositoryInjection) {
        this.packageRepositoryInjection = packageRepositoryInjection;
    }

    public CertificateAuthorityInjection getCertificateAuthorityInjection() {
        return certificateAuthorityInjection;
    }

    public void setCertificateAuthorityInjection(
            CertificateAuthorityInjection certificateAuthorityInjection) {
        this.certificateAuthorityInjection = certificateAuthorityInjection;
    }

    public void setVault(Vault vault) {
        this.vault = vault;
    }

    public OnyxiaAPI getOnyxiaAPI() {
        return onyxiaAPI;
    }

    public void setOnyxiaAPI(OnyxiaAPI onyxiaAPI) {
        this.onyxiaAPI = onyxiaAPI;
    }

    @JsonIgnoreProperties(
            value = {"server"},
            allowSetters = true)
    public static class Services {

        public static enum AuthenticationMode {
            @JsonProperty("impersonate")
            IMPERSONATE,
            @JsonProperty("admin")
            ADMIN,
            @JsonProperty("user")
            USER
        }

        private Service.ServiceType type;
        private boolean singleNamespace = true;
        private boolean allowNamespaceCreation = true;
        private boolean userNamespace = true;
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
        private String allowedURIPattern = "^https://";
        private Quotas quotas = new Quotas();
        private DefaultConfiguration defaultConfiguration = new DefaultConfiguration();
        private K8sPublicEndpoint k8sPublicEndpoint = new K8sPublicEndpoint();
        private CustomInitScript customInitScript = new CustomInitScript();

        public static class DefaultConfiguration {
            private boolean IPProtection = false;
            private boolean networkPolicy = false;
            private List<Object> from = new ArrayList<>();
            private List<Object> tolerations = new ArrayList<>();
            private Object nodeSelector;
            private Object startupProbe;
            private Kafka kafka = new Kafka();
            private Sliders sliders = new Sliders();
            private Resources resources = new Resources();

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

            public void setFrom(List<Object> from) {
                this.from = from;
            }

            public List<Object> getFrom() {
                return from;
            }

            public void setTolerations(List<Object> tolerations) {
                this.tolerations = tolerations;
            }

            public List<Object> getTolerations() {
                return tolerations;
            }

            public void setNodeSelector(Object nodeSelector) {
                this.nodeSelector = nodeSelector;
            }

            public Object getNodeSelector() {
                return nodeSelector;
            }

            public void setStartupProbe(Object startupProbe) {
                this.startupProbe = startupProbe;
            }

            public Object getStartupProbe() {
                return startupProbe;
            }

            public Kafka getKafka() {
                return kafka;
            }

            public void setKafka(Kafka kafka) {
                this.kafka = kafka;
            }

            public Sliders getSliders() {
                return sliders;
            }

            public void setSliders(Sliders sliders) {
                this.sliders = sliders;
            }

            public Resources getResources() {
                return resources;
            }

            public void setResources(Resources resources) {
                this.resources = resources;
            }

            public static class Kafka {
                @JsonProperty("URL")
                private String url;

                private String topicName;

                public String getUrl() {
                    return url;
                }

                public void setUrl(String url) {
                    this.url = url;
                }

                public String getTopicName() {
                    return topicName;
                }

                public void setTopicName(String topicName) {
                    this.topicName = topicName;
                }
            }

            public static class Sliders {

                Slider cpu;
                Slider memory;
                Slider gpu;
                Slider disk;

                public Slider getCpu() {
                    return cpu;
                }

                public void setCpu(Slider cpu) {
                    this.cpu = cpu;
                }

                public Slider getMemory() {
                    return memory;
                }

                public void setMemory(Slider memory) {
                    this.memory = memory;
                }

                public Slider getGpu() {
                    return gpu;
                }

                public void setGpu(Slider gpu) {
                    this.gpu = gpu;
                }

                public Slider getDisk() {
                    return disk;
                }

                public void setDisk(Slider disk) {
                    this.disk = disk;
                }

                public static class Slider {

                    Integer sliderMin;
                    Integer sliderMax;
                    Integer sliderStep;
                    String sliderUnit;

                    public Integer getSliderMin() {
                        return sliderMin;
                    }

                    public void setSliderMin(Integer sliderMin) {
                        this.sliderMin = sliderMin;
                    }

                    public Integer getSliderMax() {
                        return sliderMax;
                    }

                    public void setSliderMax(Integer sliderMax) {
                        this.sliderMax = sliderMax;
                    }

                    public Integer getSliderStep() {
                        return sliderStep;
                    }

                    public void setSliderStep(Integer sliderStep) {
                        this.sliderStep = sliderStep;
                    }

                    public String getSliderUnit() {
                        return sliderUnit;
                    }

                    public void setSliderUnit(String sliderUnit) {
                        this.sliderUnit = sliderUnit;
                    }
                }
            }

            public static class Resources {
                private String cpuRequest;
                private String cpuLimit;
                private String memoryRequest;
                private String memoryLimit;
                private String disk;
                private String gpu;

                public String getCpuRequest() {
                    return cpuRequest;
                }

                public void setCpuRequest(String cpuRequest) {
                    this.cpuRequest = cpuRequest;
                }

                public String getCpuLimit() {
                    return cpuLimit;
                }

                public void setCpuLimit(String cpuLimit) {
                    this.cpuLimit = cpuLimit;
                }

                public String getMemoryRequest() {
                    return memoryRequest;
                }

                public void setMemoryRequest(String memoryRequest) {
                    this.memoryRequest = memoryRequest;
                }

                public String getMemoryLimit() {
                    return memoryLimit;
                }

                public void setMemoryLimit(String memoryLimit) {
                    this.memoryLimit = memoryLimit;
                }

                public String getDisk() {
                    return disk;
                }

                public void setDisk(String disk) {
                    this.disk = disk;
                }

                public String getGpu() {
                    return gpu;
                }

                public void setGpu(String gpu) {
                    this.gpu = gpu;
                }
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

        public CustomInitScript getCustomInitScript() {
            return customInitScript;
        }

        public void setCustomInitScript(CustomInitScript customInitScript) {
            this.customInitScript = customInitScript;
        }

        public boolean isSingleNamespace() {
            return singleNamespace;
        }

        public void setSingleNamespace(boolean singleNamespace) {
            this.singleNamespace = singleNamespace;
        }

        public boolean isAllowNamespaceCreation() {
            return allowNamespaceCreation;
        }

        public void setAllowNamespaceCreation(boolean allowNamespaceCreation) {
            this.allowNamespaceCreation = allowNamespaceCreation;
        }

        public boolean isUserNamespace() {
            return userNamespace;
        }

        public void setUserNamespace(boolean userNamespace) {
            this.userNamespace = userNamespace;
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

        public String getAllowedURIPattern() {
            return allowedURIPattern;
        }

        public void setAllowedURIPattern(String allowedURIPattern) {
            this.allowedURIPattern = allowedURIPattern;
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

        public K8sPublicEndpoint getK8sPublicEndpoint() {
            return k8sPublicEndpoint;
        }

        public void setK8sPublicEndpoint(K8sPublicEndpoint k8sPublicEndpoint) {
            this.k8sPublicEndpoint = k8sPublicEndpoint;
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

    @Schema(description = "Cloudshell data and health")
    public static class Data {

        private Atlas atlas;

        @JsonProperty("S3")
        private S3 s3;

        public Atlas getAtlas() {
            return atlas;
        }

        public void setAtlas(Atlas atlas) {
            this.atlas = atlas;
        }

        public S3 getS3() {
            return s3;
        }

        public void setS3(S3 s3) {
            this.s3 = s3;
        }
    }

    @Schema(description = "Cloudshell data and health")
    public static class Atlas {

        @JsonProperty("URL")
        private String url;

        private KeycloakParams keycloakParams;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void setKeycloakParams(KeycloakParams keycloakParams) {
            this.keycloakParams = keycloakParams;
        }

        public KeycloakParams getKeycloakParams() {
            return keycloakParams;
        }
    }

    @Schema(description = "Vault Configuration")
    public static class Vault {

        @JsonProperty("URL")
        private String url;

        private String kvEngine;
        private String role;

        private KeycloakParams keycloakParams;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getKvEngine() {
            return kvEngine;
        }

        public void setKvEngine(String kvEngine) {
            this.kvEngine = kvEngine;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public void setKeycloakParams(KeycloakParams keycloakParams) {
            this.keycloakParams = keycloakParams;
        }

        public KeycloakParams getKeycloakParams() {
            return keycloakParams;
        }
    }

    public static class K8sPublicEndpoint {

        @JsonProperty("URL")
        private String url;

        private KeycloakParams keycloakParams;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void setKeycloakParams(KeycloakParams keycloakParams) {
            this.keycloakParams = keycloakParams;
        }

        public KeycloakParams getKeycloakParams() {
            return keycloakParams;
        }
    }

    public static class CustomInitScript {

        @JsonProperty("URL")
        private String url;

        private String checksum;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getChecksum() {
            return checksum;
        }

        public void setChecksum(String checksum) {
            this.checksum = checksum;
        }
    }

    @Schema(description = "Configuration to be used by the S3 client associated to Onyxia")
    public static class S3 {
        private String type;

        @JsonProperty("URL")
        private String url;

        private String region;
        private String roleARN;
        private String roleSessionName;
        private String bucketPrefix;
        private String groupBucketPrefix;
        private String bucketClaim = "preferred_username";
        private long defaultDurationSeconds;
        private KeycloakParams keycloakParams;
        private Monitoring monitoring;
        private boolean acceptBucketCreation = true;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
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

        public long getDefaultDurationSeconds() {
            return defaultDurationSeconds;
        }

        public void setDefaultDurationSeconds(long defaultDurationSeconds) {
            this.defaultDurationSeconds = defaultDurationSeconds;
        }

        public void setMonitoring(Monitoring monitoring) {
            this.monitoring = monitoring;
        }

        public Monitoring getMonitoring() {
            return monitoring;
        }

        public void setKeycloakParams(KeycloakParams keycloakParams) {
            this.keycloakParams = keycloakParams;
        }

        public KeycloakParams getKeycloakParams() {
            return keycloakParams;
        }

        public void setAcceptBucketCreation(boolean acceptBucketCreation) {
            this.acceptBucketCreation = acceptBucketCreation;
        }

        public boolean isAcceptBucketCreation() {
            return acceptBucketCreation;
        }
    }

    public static class KeycloakParams {
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

    public static class Expose {
        private String domain;
        private String ingressClassName;

        private boolean ingress = true;

        private boolean route = false;

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getDomain() {
            return domain;
        }

        public void setIngressClassName(String ingressClassName) {
            this.ingressClassName = ingressClassName;
        }

        public String getIngressClassName() {
            return ingressClassName;
        }

        public void setIngress(boolean ingress) {
            this.ingress = ingress;
        }

        public boolean getIngress() {
            return ingress;
        }

        public void setRoute(boolean route) {
            this.route = route;
        }

        public boolean getRoute() {
            return route;
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
        @Schema(description = "Cloudshell data and health")
        private String baseURL;

        public String getBaseURL() {
            return baseURL;
        }

        public void setBaseURL(String baseURL) {
            this.baseURL = baseURL;
        }
    }

    public static class ProxyInjection {
        @Schema(description = "httpProxyUrl to inject in helm values")
        private String httpProxyUrl;

        @Schema(description = "httpsProxyUrl to inject in helm values")
        private String httpsProxyUrl;

        @Schema(description = "noProxy to inject in helm values")
        private String noProxy;

        public String getHttpProxyUrl() {
            return httpProxyUrl;
        }

        public void setHttpProxyUrl(String httpProxyUrl) {
            this.httpProxyUrl = httpProxyUrl;
        }

        public String getHttpsProxyUrl() {
            return httpsProxyUrl;
        }

        public void setHttpsProxyUrl(String httpsProxyUrl) {
            this.httpsProxyUrl = httpsProxyUrl;
        }

        public String getNoProxy() {
            return noProxy;
        }

        public void setNoProxy(String noProxy) {
            this.noProxy = noProxy;
        }
    }

    public static class CertificateAuthorityInjection {

        /** Will be deprecated in the next releases cacerts as string will replace */
        @Schema(description = "List of crt encoded in base64")
        @Deprecated
        private List<String> crts = new ArrayList<>();

        private String cacerts;
        private String pathToCaBundle;

        public List<String> getCrts() {
            return crts;
        }

        public void setCrts(List<String> crts) {
            this.crts = crts;
        }

        public String getCacerts() {
            return cacerts;
        }

        public void setCacerts(String cacerts) {
            this.cacerts = cacerts;
        }

        public String getPathToCaBundle() {
            return pathToCaBundle;
        }

        public void setPathToCaBundle(String pathToCaBundle) {
            this.pathToCaBundle = pathToCaBundle;
        }
    }

    public static class PackageRepositoryInjection {
        @Schema(description = "httpProxyUrl to inject in helm values")
        private String cranProxyUrl;

        @Schema(description = "httpProxyUrl to inject in helm values")
        private String condaProxyUrl;

        @Schema(description = "httpProxyUrl to inject in helm values")
        private String pypiProxyUrl;

        public String getCranProxyUrl() {
            return cranProxyUrl;
        }

        public void setCranProxyUrl(String cranProxyUrl) {
            this.cranProxyUrl = cranProxyUrl;
        }

        public String getCondaProxyUrl() {
            return condaProxyUrl;
        }

        public void setCondaProxyUrl(String condaProxyUrl) {
            this.condaProxyUrl = condaProxyUrl;
        }

        public String getPypiProxyUrl() {
            return pypiProxyUrl;
        }

        public void setPypiProxyUrl(String pypiProxyUrl) {
            this.pypiProxyUrl = pypiProxyUrl;
        }
    }

    @Schema(description = "Cloudshell data and health")
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
