package fr.insee.onyxia.model.region;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.insee.onyxia.model.service.quota.Quota;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Services services = new Services();

    @Schema(description = "")
    private OnyxiaAPI onyxiaAPI;

    @Schema(description = "")
    private Data data = new Data();

    @Schema(description = "")
    private Vault vault;

    @Schema(description = "")
    private Git git;

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

    public void setVault(Vault vault) {
        this.vault = vault;
    }

    public Git getGit() {
        return git;
    }

    public void setGit(Git git) {
        this.git = git;
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

        private boolean singleNamespace = true;
        private boolean allowNamespaceCreation = true;
        private Map<String, String> namespaceLabels = new HashMap<>();
        private Map<String, String> namespaceAnnotations = new HashMap<>();
        private boolean userNamespace = true;
        private String namespacePrefix = "user-";
        private String groupNamespacePrefix = "projet-";
        private String usernamePrefix;
        private String groupPrefix;
        private AuthenticationMode authenticationMode = AuthenticationMode.SERVICEACCOUNT;
        private Expose expose;
        private Server server;
        private Monitoring monitoring;
        private String allowedURIPattern = "^https://";
        private Quotas quotas = new Quotas();

        /***
         * @Deprecated since v3
         * Should no longer be used. If used, a check will fail at startup and crash the app.
         * @See V3Checks
         */
        private Object defaultConfiguration = null;

        private K8sPublicEndpoint k8sPublicEndpoint = new K8sPublicEndpoint();

        private NamespaceAnnotationsDynamic namespaceAnnotationsDynamic =
                new NamespaceAnnotationsDynamic();

        public boolean isSingleNamespace() {
            return singleNamespace;
        }

        public void setSingleNamespace(boolean singleNamespace) {
            this.singleNamespace = singleNamespace;
        }

        public void setDefaultConfiguration(Object defaultConfiguration) {
            this.defaultConfiguration = defaultConfiguration;
        }

        public Object getDefaultConfiguration() {
            return defaultConfiguration;
        }

        public boolean isAllowNamespaceCreation() {
            return allowNamespaceCreation;
        }

        public void setAllowNamespaceCreation(boolean allowNamespaceCreation) {
            this.allowNamespaceCreation = allowNamespaceCreation;
        }

        public NamespaceAnnotationsDynamic getNamespaceAnnotationsDynamic() {
            return namespaceAnnotationsDynamic;
        }

        public void setNamespaceAnnotationsDynamic(
                NamespaceAnnotationsDynamic namespaceAnnotationsDynamic) {
            this.namespaceAnnotationsDynamic = namespaceAnnotationsDynamic;
        }

        public Map<String, String> getNamespaceLabels() {
            return namespaceLabels;
        }

        public void getNamespaceLabels(Map<String, String> namespaceLabels) {
            this.namespaceLabels = namespaceLabels;
        }

        public Map<String, String> getNamespaceAnnotations() {
            return namespaceAnnotations;
        }

        public void getNamespaceAnnotations(Map<String, String> namespaceAnnotations) {
            this.namespaceAnnotations = namespaceAnnotations;
        }

        public boolean isUserNamespace() {
            return userNamespace;
        }

        public void setUserNamespace(boolean userNamespace) {
            this.userNamespace = userNamespace;
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

        public Monitoring getMonitoring() {
            return monitoring;
        }

        public void setMonitoring(Monitoring monitoring) {
            this.monitoring = monitoring;
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

        public String getGroupNamespacePrefix() {
            return groupNamespacePrefix;
        }

        public void setGroupNamespacePrefix(String groupNamespacePrefix) {
            this.groupNamespacePrefix = groupNamespacePrefix;
        }

        public String getGroupPrefix() {
            return groupPrefix;
        }

        public void setGroupPrefix(String groupPrefix) {
            this.groupPrefix = groupPrefix;
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

        public static enum AuthenticationMode {
            @JsonProperty("impersonate")
            IMPERSONATE,
            @JsonProperty("serviceAccount")
            @JsonAlias("admin")
            SERVICEACCOUNT,

            @JsonProperty("tokenPassthrough")
            TOKEN_PASSTHROUGH
        }

        public static class NamespaceAnnotationsDynamic {
            private boolean enabled = true;

            private List<String> userAttributes = new ArrayList<>();

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public List<String> getUserAttributes() {
                return userAttributes;
            }

            public void setUserAttributes(List<String> userAttributes) {
                this.userAttributes = userAttributes;
            }
        }

        public static class Quotas {

            private boolean userEnabled = false;
            private boolean groupEnabled = false;

            @JsonProperty("user")
            private Quota userQuota;

            @JsonProperty("group")
            private Quota groupQuota;

            @JsonProperty("roles")
            private Map<String, Quota> rolesQuota = new HashMap<>();

            public boolean isUserEnabled() {
                return userEnabled;
            }

            public void setUserEnabled(boolean userEnabled) {
                this.userEnabled = userEnabled;
            }

            public boolean isGroupEnabled() {
                return groupEnabled;
            }

            public void setGroupEnabled(boolean groupEnabled) {
                this.groupEnabled = groupEnabled;
            }

            public Quota getUserQuota() {
                return userQuota;
            }

            public void setUserQuota(Quota userQuota) {
                this.userQuota = userQuota;
            }

            public Quota getGroupQuota() {
                return groupQuota;
            }

            public void setGroupQuota(Quota groupQuota) {
                this.groupQuota = groupQuota;
            }

            public void setRolesQuota(Map<String, Quota> rolesQuota) {
                this.rolesQuota = rolesQuota;
            }

            public Map<String, Quota> getRolesQuota() {
                return rolesQuota;
            }
        }
    }

    public static class Monitoring {
        @JsonProperty("URLPattern")
        private String urlPattern;

        public String getUrlPattern() {
            return urlPattern;
        }

        public void setUrlPattern(String urlPattern) {
            this.urlPattern = urlPattern;
        }
    }

    @Schema(description = "")
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

    @Schema(description = "Vault Configuration")
    public static class Vault {

        @JsonProperty("URL")
        private String url;

        private String kvEngine;
        private String role;
        private String authPath = "jwt";

        private String prefix;

        private String groupPrefix;

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public void setGroupPrefix(String groupPrefix) {
            this.groupPrefix = groupPrefix;
        }

        public String getGroupPrefix() {
            return groupPrefix;
        }

        private OIDCConfiguration oidcConfiguration = null;

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

        public String getAuthPath() {
            return authPath;
        }

        public void setAuthPath(String authPath) {
            this.authPath = authPath;
        }

        public OIDCConfiguration getOidcConfiguration() {
            return oidcConfiguration;
        }

        public void setOidcConfiguration(OIDCConfiguration oidcConfiguration) {
            this.oidcConfiguration = oidcConfiguration;
        }
    }

    @Schema(description = "Git Configuration")
    public static class Git {

        private String type;

        @JsonProperty("URL")
        private String url;

        private OIDCConfiguration oidcConfiguration = null;

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

        public OIDCConfiguration getOidcConfiguration() {
            return oidcConfiguration;
        }

        public void setOidcConfiguration(OIDCConfiguration oidcConfiguration) {
            this.oidcConfiguration = oidcConfiguration;
        }
    }

    public static class K8sPublicEndpoint {

        @JsonProperty("URL")
        private String url;

        private OIDCConfiguration oidcConfiguration = null;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public OIDCConfiguration getOidcConfiguration() {
            return oidcConfiguration;
        }

        public void setOidcConfiguration(OIDCConfiguration oidcConfiguration) {
            this.oidcConfiguration = oidcConfiguration;
        }
    }

    @Schema(description = "Configuration to be used by the S3 client associated to Onyxia")
    public static class S3 {

        @JsonProperty("URL")
        private String url;

        private String region;

        private boolean pathStyleAccess = true;

        private Sts sts;

        private WorkingDirectory workingDirectory;

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

        public boolean getPathStyleAccess() {
            return pathStyleAccess;
        }

        public void setPathStyleAccess(boolean pathStyleAccess) {
            this.pathStyleAccess = pathStyleAccess;
        }

        public Sts getSts() {
            return sts;
        }

        public void setSts(Sts sts) {
            this.sts = sts;
        }

        public WorkingDirectory getWorkingDirectory() {
            return workingDirectory;
        }

        public void setWorkingDirectory(WorkingDirectory workingDirectory) {
            this.workingDirectory = workingDirectory;
        }
    }

    public static class Sts {

        @JsonProperty("URL")
        private String url;

        private long durationSeconds;

        private OIDCConfiguration oidcConfiguration = null;

        private Role role;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public long getDurationSeconds() {
            return durationSeconds;
        }

        public void setDurationSeconds(long durationSeconds) {
            this.durationSeconds = durationSeconds;
        }

        public OIDCConfiguration getOidcConfiguration() {
            return oidcConfiguration;
        }

        public void setOidcConfiguration(OIDCConfiguration oidcConfiguration) {
            this.oidcConfiguration = oidcConfiguration;
        }

        public Role getRole() {
            return role;
        }

        public void setRole(Role role) {
            this.role = role;
        }
    }

    public static class Role {

        private String roleARN;
        private String roleSessionName;

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
    }

    public enum BucketMode {
        @JsonProperty("multi")
        MULTI,
        @JsonProperty("shared")
        SHARED
    }

    public static class WorkingDirectory {

        private BucketMode bucketMode;
        private String bucketName;
        private String prefix;
        private String prefixGroup;
        private String bucketNamePrefix;
        private String bucketNamePrefixGroup;

        public BucketMode getBucketMode() {
            return bucketMode;
        }

        public void setBucketMode(BucketMode bucketMode) {
            this.bucketMode = bucketMode;
        }

        public String getBucketName() {
            return bucketName;
        }

        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefixGroup() {
            return prefixGroup;
        }

        public void setPrefixGroup(String prefixGroup) {
            this.prefixGroup = prefixGroup;
        }

        public String getBucketNamePrefix() {
            return bucketNamePrefix;
        }

        public void setBucketNamePrefix(String bucketNamePrefix) {
            this.bucketNamePrefix = bucketNamePrefix;
        }

        public String getBucketNamePrefixGroup() {
            return bucketNamePrefixGroup;
        }

        public void setBucketNamePrefixGroup(String bucketNamePrefixGroup) {
            this.bucketNamePrefixGroup = bucketNamePrefixGroup;
        }
    }

    public static class OIDCConfiguration {

        private String issuerURI;
        private String clientID;
        private String extraQueryParams;
        private String scope;
        private String workaroundForGoogleClientSecret;

        private String audience;

        public String getIssuerURI() {
            return issuerURI;
        }

        public void setIssuerURI(String issuerURI) {
            this.issuerURI = issuerURI;
        }

        public String getClientID() {
            return clientID;
        }

        public void setClientID(String clientID) {
            this.clientID = clientID;
        }

        public String getExtraQueryParams() {
            return extraQueryParams;
        }

        public void setExtraQueryParams(String extraQueryParams) {
            this.extraQueryParams = extraQueryParams;
        }

        public String getWorkaroundForGoogleClientSecret() {
            return workaroundForGoogleClientSecret;
        }

        public void setWorkaroundForGoogleClientSecret(String workaroundForGoogleClientSecret) {
            this.workaroundForGoogleClientSecret = workaroundForGoogleClientSecret;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public void setAudience(String audience) {
            this.audience = audience;
        }

        public String getAudience() {
            return audience;
        }
    }

    public static class Expose {
        private String domain;
        private String ingressClassName;
        private boolean useDefaultCertificate = true;
        private Map<String, String> annotations = new HashMap<>();
        private boolean ingress = true;

        private boolean route = false;

        private IstioIngress istio;
        private CertManager certManager;

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getIngressClassName() {
            return ingressClassName;
        }

        public void setIngressClassName(String ingressClassName) {
            this.ingressClassName = ingressClassName;
        }

        public boolean getUseDefaultCertificate() {
            return useDefaultCertificate;
        }

        public void setUseDefaultCertificate(boolean useDefaultCertificate) {
            this.useDefaultCertificate = useDefaultCertificate;
        }

        public void setAnnotations(Map<String, String> annotations) {
            this.annotations = annotations;
        }

        public Map<String, String> getAnnotations() {
            return annotations;
        }

        public boolean getIngress() {
            return ingress;
        }

        public void setIngress(boolean ingress) {
            this.ingress = ingress;
        }

        public boolean getRoute() {
            return route;
        }

        public void setRoute(boolean route) {
            this.route = route;
        }

        public IstioIngress getIstio() {
            return istio;
        }

        public void setIstio(IstioIngress istio) {
            this.istio = istio;
        }

        public CertManager getCertManager() {
            return certManager;
        }

        public void setCertManager(CertManager certManager) {
            this.certManager = certManager;
        }
    }

    public static class IstioIngress {
        private boolean enabled = false;

        private String[] gateways = new String[0];

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String[] getGateways() {
            return gateways;
        }

        public void setGateways(String[] gateways) {
            this.gateways = gateways;
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
        @Schema(description = "")
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

        @Schema(description = "cranProxyUrl to inject in helm values")
        private String cranProxyUrl;

        @Schema(description = "condaProxyUrl to inject in helm values")
        private String condaProxyUrl;

        @Schema(description = "packageManager url to inject in helm values")
        private String packageManagerUrl;

        @Schema(description = "pypiProxyUrl to inject in helm values")
        private String pypiProxyUrl;

        public String getPackageManagerUrl() {
            return packageManagerUrl;
        }

        public void setPackageManagerUrl(String packageManagerUrl) {
            this.packageManagerUrl = packageManagerUrl;
        }

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

    public static class CertManager {
        private boolean useCertManager;

        private String certManagerClusterIssuer;

        public boolean isUseCertManager() {
            return useCertManager;
        }

        public void setUseCertManager(boolean useCertManager) {
            this.useCertManager = useCertManager;
        }

        public String getCertManagerClusterIssuer() {
            return certManagerClusterIssuer;
        }

        public void setCertManagerClusterIssuer(String certManagerClusterIssuer) {
            this.certManagerClusterIssuer = certManagerClusterIssuer;
        }
    }
}
